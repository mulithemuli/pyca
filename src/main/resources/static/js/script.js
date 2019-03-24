(function(pyca, $, undefined) {

    let elements = {
        back: $('#back'),
        content: $('#content'),
        author: $('#author'),
        videos: $('#videos'),
        mainContent: $('#main_content'),
        addVideoButton: $('#add_video_button')
    };

    elements.back.on('click', e => {
        e.preventDefault();
        let home = location.toString().replace(location.pathname, '');
        history.pushState({ url : home }, null, home);
        mainPage();
    });

    let mainPage = () => {
        $('.fullscreen-container').remove();
        elements.content.show();
        elements.addVideoButton.show();
        elements.back.hide();
        updateSeenComments();
    };

    $(elements.videos).click('a.video-details', e => {
        e.preventDefault();
        let link = $(e.target);
        if (!link.is('a')) {
            link = link.parents('a');
        }
        loadVideo(link.attr('href'));
        history.pushState({ url: location.toString().replace(location.pathname, '') + link.attr('href') }, null,  link.attr('href'));
    });

    let loadVideo = video => {
        $.get('/api' + video).done((data, textStatus, xhr) => {
            if (xhr.status !== 200) {
                return;
            }
            let videoInfo = $(Mustache.render(templates.detailVideoContainer, data));
            elements.mainContent.append(videoInfo);
            elements.addVideoButton.hide();
            elements.content.hide();
            elements.back.removeClass('hide');
            elements.back.show();
            let comments = $('#comments');
            let countComments = 0;
            let enhanceTime = commentEl => {
                let added = $('.added', commentEl);
                added
                    .data('time', added.text())
                    .attr('title', moment(added.text()).format('YYYY-MM-DD, HH:mm:ss'))
                    .text(moment(added.text()).fromNow());
            };
            $.get('/api/comments/' + data.videoId).done(data => {
                $.each(data, (i, comment) => {
                    let commentEl = $(Mustache.render(templates.comment, comment));
                    comments.append(commentEl);
                    enhanceTime(commentEl);
                    countComments++;
                });
                data.comments = countComments;
            });
            updateSeenComments(data);
            $(templates.commentInput).insertAfter($('h3', videoInfo));
            let addComment = $('#add_comment');
            let comment = $('#comment');
            addComment.on('click', e => {
                e.preventDefault();
                let author = storage.get('author');
                let text = comment.val();
                if (!author || !text) {
                    M.toast({html: 'Comment and Author must be set.'});
                }
                let req = {
                    author: author,
                    text: text,
                    videoId: data.videoId
                };
                $.ajax({
                    url: '/api/comment',
                    type: 'POST',
                    contentType: 'application/json',
                    data: JSON.stringify(req),
                    dataType: 'json',
                    processData: false
                }).done((res, textStatus, xhr) => {
                    comment.val('');
                    let newComment = $(Mustache.render(templates.comment, res));
                    comments.prepend(newComment);
                    enhanceTime(newComment);
                });
                saveAuthor();
            });
        });
    };

    $('a.add-video').on('click', e => {
        e.preventDefault();
        let addVideoContainer = document.getElementById('add_video_container');
        if (addVideoContainer) {
            $('#url', addVideoContainer).focus();
            return;
        }
        addVideoContainer = $(templates.addVideo);
        elements.content.prepend(addVideoContainer);
        let title = $('h6', addVideoContainer);
        let video = $('.video-container', addVideoContainer);
        let save = $('a.save-video', addVideoContainer);
        $('#url').on('change', e => {
            save.off('click').on('click', e => e.preventDefault());
            title.addClass('hide');
            video.addClass('hide');
            $.get('/api/video', { url: $(e.target).val()}).done(data => {
                title.text(data.title).removeClass('hide');
                video
                    .html('')
                    .append($('<iframe width="853" height="480">')
                        .attr({
                            src: data.embed + '?rel=0',
                            frameborder: 0,
                            allowfullscreen: ''
                        }))
                    .removeClass('hide');
                save.on('click', e => {
                    e.preventDefault();
                    if (!storage.get('author')) {
                        M.toast({html: 'You need to specify who you are.'});
                        return;
                    }
                    data.author = storage.get('author');
                    $.ajax({
                        url: '/api/video',
                        type: 'POST',
                        contentType: 'application/json',
                        data: JSON.stringify(data),
                        dataType: 'json',
                        processData: false
                    }).done((res, textStatus, xhr) => {
                        if (xhr.status === 200) {
                            elements.videos.prepend($(Mustache.render(templates.listVideo, res)));
                        } else if (xhr.status === 304) {
                            let item = $('a[name=' + data.videoId + ']');
                            item.addClass('tag-existing');
                            setTimeout(() => item.removeClass('tag-existing'), 1000);
                        }
                        $(addVideoContainer).remove();
                    });
                    saveAuthor();
                })
            });
        });
    });

    elements.author.on('input', e => {
        storage.set('author', $(e.target).val());
    });

    let storage = new function() {
        let store = JSON.parse(localStorage.getItem('pyca.store') || '{}');
        this.set = (k, v) => {
            store[k] = v;
            localStorage.setItem('pyca.store', JSON.stringify(store));
        };
        this.get = k => store[k];
    };

    let saveAuthor = () => {
        $.ajax({
            url: '/api/author',
            type: 'PUT',
            contentType: 'application/json',
            data: JSON.stringify({ name: storage.get('author') }),
            dataType: 'json',
            processData: false
        });
    };

    let markUnseenComments = () => {
        if (!storage.get('author')) {
            return;
        }
        $.get('/api/author/' + storage.get('author')).done((data, textStatus, xhr) => {
            if (xhr.status !== 200) {
                return;
            }
            $('a', elements.videos).each((i, el) => {
                let seenComments = data.seenComments[$(el).data('videoId')] || 0;
                let comments = $('.count-comments', el).text();
                if (comments - seenComments > 0) {
                    $('.new-comments', el).removeClass('hide').text(comments - seenComments);
                }
            });
        });
    };

    let updateSeenComments = video => {
        if (!storage.get('author')) {
            return;
        }
        let seenComments = {};
        seenComments[video.videoId] = video.comments;
        $.ajax({
            url: '/api/author',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ name: storage.get('author'), seenComments: seenComments }),
            dataType: 'json',
            processData: false
        }).done(() => {
            markUnseenComments();
        });
    };

    $(() => {
        elements.author.val(storage.get('author'));
        M.updateTextFields();
        $.get('/api/videos').done(videos => {
            $.each(videos, (i, video) => {
                elements.videos.append($(Mustache.render(templates.listVideo, video)));
            });
            markUnseenComments();
        });
        if (location.pathname !== '/') {
            loadVideo(location.pathname);
        }
    });

    setInterval(() => {
        let added = $('.added');
        added.each((i, el) => {
            let $el = $(el);
            let time = $el.data('time');
            if (!time) {
                return;
            }
            $el.text(moment(time).fromNow());
        }, 500);
    });

    history.replaceState({ url: location.href }, null, location.href);

    onpopstate = e => {
        let url = new URL(e.state.url);
        if (url.pathname === '/') {
            mainPage();
        } else {
            loadVideo(url.pathname);
        }
    };

    let templates = {
        addVideo: '<div id="add_video_container">\
    <div class="input-field">\
        <i class="material-icons prefix">music_video\n</i>\
        <input id="url" type="text" class="validate" required>\
        <label for="url">Video URL</label>\
    </div>\
    <h6 class="hide"></h6> \
    <div class="video-container hide"></div>\
    <div class="actions"><a class="btn waves-effect waves-light light-blue darken-2 save-video"><i class="material-icons right">save</i>Save </a></div>\
</div>',
        listVideo: '<a class="collection-item video-details" href="/video/{{videoId}}" name="{{videoId}}" data-video-id="{{videoId}}">\
    <span class="badge count-comments">{{comments}}</span>\
    <span class="badge new green lighten-2 hide new-comments">0</span>\
    <div class="title light-blue-text darken-2"><strong>{{title}}</strong></div>\
    <div class="light-blue-text darken-1">{{author}}</div>\
</a>',
        detailVideoContainer: '<div class="fullscreen-container container">\
    <h2><a href="{{url}}" target="_blank">{{title}}</a> <div class="author right">{{author}}</div></h2>\
    <div class="clearfix"></div> \
    <div class="video-container"><iframe src="{{embed}}" width="853" height="480" frameborder="0" allowfullscreen /></div>\
    <h3>Comments</h3>\
    <ul id="comments" class="collection"></ul>\
</div>',
        commentInput: '<div class="row">\
    <div class="input-field col s10 m11">\
        <i class="material-icons prefix">comment</i>\
        <textarea id="comment" class="validate materialize-textarea" required></textarea>\
        <label for="comment">New Comment</label>\
    </div>\
    <div class="input-field col s2 m1" id="add_video_button">\
        <a class="btn-floating waves-effect waves-light light-blue darken-2 add-comment" id="add_comment"><i class="material-icons">send</i></a>\
    </div>\
</div>',
        comment: '<li class="collection-item">\
    <div class="title">\
        <span class="author">{{author}}</span>\
        <span class="added right">{{dateAdd}}</span>\
    </div>\
    <div class="comment">{{text}}</div>\
</li>'
    }
}(window.pyca = window.pyca || {}, jQuery));