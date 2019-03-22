package at.muli.pyca.controller;

import at.muli.pyca.bo.YouTubeInfo;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URL;

@Log4j2
@RestController
@RequestMapping("/api")
public class YouTubeController {

    @RequestMapping(path = "/video", method = RequestMethod.GET)
    public ResponseEntity<YouTubeInfo> loadVideoInfo(@RequestParam("url") String url) {
        try {
            Document document = Jsoup.connect(url).get();
            Elements elements = document.select("title");
            String title = elements.first().text().replace(" - YouTube", "");
            if ("YouTube".equals(title.trim()) || "".equals(title.trim())) {
                return ResponseEntity.notFound().build();
            }
            URL originalUrl = new URL(url);
            String videoId = null;
            for (String paramValue : originalUrl.getQuery().split("&")) {
                String[] param = paramValue.split("=");
                if ("v".equals(param[0])) {
                    videoId = param[1];
                    break;
                }
            }
            String embed = String.format("%s://%s/embed/%s", originalUrl.getProtocol(), originalUrl.getHost(), videoId);
            return ResponseEntity.ok(YouTubeInfo.builder().embed(embed).title(title).url(url).videoId(videoId).build());
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(path = "/video", method = RequestMethod.POST)
    public ResponseEntity<YouTubeInfo> saveVideo(@RequestBody YouTubeInfo youTubeInfo) {
        return ResponseEntity.ok(youTubeInfo);
    }
}
