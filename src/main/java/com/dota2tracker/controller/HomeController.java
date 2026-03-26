package com.dota2tracker.controller;

import com.dota2tracker.dto.PlayerMatchStatDto;
import com.dota2tracker.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** MVC-контролер для Thymeleaf-шаблонів. Обробляє запити від браузера. */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final PlayerService playerService;

    /** GET / - головна сторінка зі списком гравців. */
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("players", playerService.getAllPlayers());
        return "index";
    }

    /**
     * GET /players/{id} - сторінка статистики гравця з опціональним фільтром по часу.
     * Також формує списки для шести Chart.js-графіків (KDA, kills, deaths, assists, GPM, XPM).
     *
     * @param id     внутрішній ID гравця в БД
     * @param period фільтр часу: LAST_WEEK / LAST_MONTH / LAST_YEAR / ALL_TIME
     */
    @GetMapping("/players/{id}")
    public String playerStats(
            @PathVariable Long id,
            @RequestParam(name = "period", defaultValue = "ALL_TIME") String period,
            Model model) {

        List<PlayerMatchStatDto> stats = playerService.getPlayerStats(id, period);

        // Chart.js малює зліва направо - потрібен хронологічний порядок (старіші спочатку)
        List<PlayerMatchStatDto> chrono = new ArrayList<>(stats);
        Collections.reverse(chrono);

        List<String>  chartLabels  = chrono.stream().map(PlayerMatchStatDto::formattedDate).toList();
        List<Integer> killsList    = chrono.stream().map(PlayerMatchStatDto::kills).toList();
        List<Integer> deathsList   = chrono.stream().map(PlayerMatchStatDto::deaths).toList();
        List<Integer> assistsList  = chrono.stream().map(PlayerMatchStatDto::assists).toList();
        List<Integer> gpmList      = chrono.stream().map(PlayerMatchStatDto::gpm).toList();
        List<Integer> xpmList      = chrono.stream().map(PlayerMatchStatDto::xpm).toList();
        // KDA за матч: (kills + assists) / max(deaths, 1)
        List<Double>  kdaList      = chrono.stream()
                .map(s -> {
                    int k = s.kills()   == null ? 0 : s.kills();
                    int d = s.deaths()  == null ? 0 : s.deaths();
                    int a = s.assists() == null ? 0 : s.assists();
                    return Math.round(((k + a) / (double) Math.max(d, 1)) * 100.0) / 100.0;
                })
                .toList();

        model.addAttribute("stats",        stats);
        model.addAttribute("aggregated",   playerService.getAggregatedStats(id, period));
        model.addAttribute("playerId",     id);
        model.addAttribute("period",       period);
        model.addAttribute("chartLabels",  chartLabels);
        model.addAttribute("killsList",    killsList);
        model.addAttribute("deathsList",   deathsList);
        model.addAttribute("assistsList",  assistsList);
        model.addAttribute("gpmList",      gpmList);
        model.addAttribute("xpmList",      xpmList);
        model.addAttribute("kdaList",      kdaList);

        return "stats";
    }

    /**
     * POST /players/{id}/delete - видалення гравця через HTML-форму.
     * Браузерні форми не підтримують метод DELETE, тому використовуємо POST.
     */
    @PostMapping("/players/{id}/delete")
    public String deletePlayer(@PathVariable Long id) {
        playerService.deletePlayer(id);
        return "redirect:/";
    }
}
