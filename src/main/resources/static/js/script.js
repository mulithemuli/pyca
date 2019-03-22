(function(pyca, $, undefined) {

    let elements = {
        content: $('#content'),
        author: $('#author'),
        videos: $('#videos')
    };

    $('a.nav-link').on('click', e => e.preventDefault());

    $('a.add-video').on('click', e => {
        e.preventDefault();
        let addVideoContainer = document.getElementById('add_video_container');
        if (addVideoContainer) {
            $('#url', addVideoContainer).focus();
            return;
        }
        elements.content.prepend($(templates.addVideo));
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
                        console.log(xhr.status);
                        console.log(res);
                    });
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

    $(() => {
        elements.author.val(storage.get('author'));
        M.updateTextFields();
        $.get('/api/videos').done(videos => {
            $.each(videos, (i, video) => {
                elements.videos.append($(Mustache.render(templates.listVideo, video)));
                console.log(video);
            });
        });
    });

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
        listVideo: '<a class="collection-item" href="/video/{{videoId}}">\
    <span class="badge">0</span>\
    <div class="title"><strong>{{title}}</strong></div>\
    <div>{{author}}</div>\
</a>'
    }
}(window.pyca = window.pyca || {}, jQuery));