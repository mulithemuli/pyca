(function(pyca, $, undefined) {

    let elements = {
        content: $('#content'),
        author: $('#author'),
    };

    $('a.nav-link').on('click', e => e.preventDefault());

    $('a.add-video').on('click', e => {
        e.preventDefault();
        let addVideoContainer = document.getElementById('add_video_container');
        if (addVideoContainer) {
            elements.url.focus();
            return;
        }
        elements.content.prepend($(templates.addVideo));
        $('#url').on('change', e => {
            let url = $(e.target).val();
            url = url.replace(/youtube\.com\/watch\?v=/i, 'youtube.com/embed/');
            $('.video-container', addVideoContainer)
                .html('')
                .append($('<iframe width="853" height="480">')
                    .attr({
                    src: url + '?rel=0&enablejsapi=1&origin=http://localhost',
                    frameborder: 0,
                    allowfullscreen: ''
                }))
                .removeClass('hide');
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
    });

    let templates = {
        addVideo: '<div id="add_video_container">\
    <div class="input-field">\
        <i class="material-icons prefix">music_video\n</i>\
        <input id="url" type="text" class="validate" required>\
        <label for="url">Video URL</label>\
    </div>\
    <div class="video-container hide"></div>\
    <div class="actions"><a class="btn waves-effect waves-light light-blue darken-2"><i class="material-icons right">save</i>Save </a></div>\
</div>'
    }
}(window.pyca = window.pyca || {}, jQuery));