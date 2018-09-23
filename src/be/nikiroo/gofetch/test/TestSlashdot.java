package be.nikiroo.gofetch.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import be.nikiroo.gofetch.support.Slashdot;
import be.nikiroo.gofetch.support.Type;

public class TestSlashdot extends TestBase {

	static private Map<URL, File> getMap() throws MalformedURLException {
		Map<URL, File> map = new HashMap<URL, File>();

		map.put(new URL("https://slashdot.org/"), new File("index.html"));

		map.put(new URL(
				"https://developers.slashdot.org/story/18/09/06/2024232/software-developers-are-now-more-valuable-to-companies-than-money-says-survey"),
				new File(
						"developers.slashdot.org/story_18_09_06_2024232_software-developers-are-now-more-valuable-to-companies-than-money-says-survey.html"));
		map.put(new URL(
				"https://games.slashdot.org/story/18/09/06/1921222/eve-online-studio-acquired-by-korean-mmo-maker"),
				new File(
						"games.slashdot.org/story_18_09_06_1921222_eve-online-studio-acquired-by-korean-mmo-maker.html"));
		map.put(new URL(
				"https://games.slashdot.org/story/18/09/06/2146237/valve-explains-how-it-decides-whos-a-straight-up-troll-publishing-video-games-on-steam"),
				new File(
						"games.slashdot.org/story_18_09_06_2146237_valve-explains-how-it-decides-whos-a-straight-up-troll-publishing-video-games-on-steam.html"));
		map.put(new URL(
				"https://hardware.slashdot.org/story/18/09/06/1719243/robot-boat-sails-into-history-by-finishing-atlantic-crossing"),
				new File(
						"hardware.slashdot.org/story_18_09_06_1719243_robot-boat-sails-into-history-by-finishing-atlantic-crossing.html"));
		map.put(new URL(
				"https://hardware.slashdot.org/story/18/09/06/2058201/mit-graduate-creates-robot-that-swims-through-pipes-to-find-out-if-theyre-leaking"),
				new File(
						"hardware.slashdot.org/story_18_09_06_2058201_mit-graduate-creates-robot-that-swims-through-pipes-to-find-out-if-theyre-leaking.html"));
		map.put(new URL(
				"https://it.slashdot.org/story/18/09/07/0247228/380000-card-payments-compromised-in-british-airways-breach"),
				new File(
						"it.slashdot.org/story_18_09_07_0247228_380000-card-payments-compromised-in-british-airways-breach.html"));
		map.put(new URL(
				"https://mobile.slashdot.org/story/18/09/06/235254/icelanders-seek-to-keep-remote-nordic-peninsula-digital-free"),
				new File(
						"mobile.slashdot.org/story_18_09_06_235254_icelanders-seek-to-keep-remote-nordic-peninsula-digital-free.html"));
		map.put(new URL(
				"https://news.slashdot.org/story/18/09/06/1558206/computer-chips-are-still-made-in-usa"),
				new File(
						"news.slashdot.org/story_18_09_06_1558206_computer-chips-are-still-made-in-usa.html"));
		map.put(new URL(
				"https://news.slashdot.org/story/18/09/06/2043213/professor-who-coined-term-net-neutrality-thinks-its-time-to-break-up-facebook"),
				new File(
						"news.slashdot.org/story_18_09_06_2043213_professor-who-coined-term-net-neutrality-thinks-its-time-to-break-up-facebook.html"));
		map.put(new URL(
				"https://politics.slashdot.org/story/18/09/06/2137245/blockchains-are-not-safe-for-voting-concludes-nap-report"),
				new File(
						"politics.slashdot.org/story_18_09_06_2137245_blockchains-are-not-safe-for-voting-concludes-nap-report.html"));
		map.put(new URL(
				"https://science.slashdot.org/story/18/09/06/2153223/study-finds-probiotics-not-as-beneficial-for-gut-health-as-previously-thought"),
				new File(
						"science.slashdot.org/story_18_09_06_2153223_study-finds-probiotics-not-as-beneficial-for-gut-health-as-previously-thought.html"));
		map.put(new URL(
				"https://tech.slashdot.org/story/18/09/06/1839242/google-investigating-issue-with-blurry-fonts-on-new-chrome-69"),
				new File(
						"tech.slashdot.org/story_18_09_06_1839242_google-investigating-issue-with-blurry-fonts-on-new-chrome-69.html"));
		map.put(new URL(
				"https://tech.slashdot.org/story/18/09/06/1954253/400000-websites-vulnerable-through-exposed-git-directories"),
				new File(
						"tech.slashdot.org/story_18_09_06_1954253_400000-websites-vulnerable-through-exposed-git-directories.html"));
		map.put(new URL(
				"https://tech.slashdot.org/story/18/09/06/205221/ive-seen-the-future-of-consumer-ai-and-it-doesnt-have-one"),
				new File(
						"tech.slashdot.org/story_18_09_06_205221_ive-seen-the-future-of-consumer-ai-and-it-doesnt-have-one.html"));
		map.put(new URL(
				"https://yro.slashdot.org/story/18/09/06/1651255/tor-browser-gets-a-redesign-switches-to-new-firefox-quantum-engine"),
				new File(
						"yro.slashdot.org/story_18_09_06_1651255_tor-browser-gets-a-redesign-switches-to-new-firefox-quantum-engine.html"));

		return map;
	}

	public TestSlashdot(String[] args) {
		super(new Slashdot() {
			@Override
			protected InputStream open(URL url) throws IOException {
				return doOpen(this, getMap(), url);
			}

			@Override
			public Type getType() {
				return Type.SLASHDOT;
			}
		}, args);
	}
}
