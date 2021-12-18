package pairmatching.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class MatchResults {
	private List<MatchResult> matchResults = new ArrayList<>();

	public List<MatchResult> getMatchResults() {
		return matchResults;
	}

	public boolean hasMatchResultsByMission(Course course, Mission mission) {
		return matchResults.stream()
							.filter(match -> match.getMission().equals(mission))
							.collect(Collectors.toList())
							.size() > 0;
	}

	public void match(CrewRepository crewsByString, Mission mission, int trial) {
		if (trial > 2) {
			throw new RuntimeException("[ERROR] 3회 매칭에 실패하였습니다");
		}

		List<String> shuffle = crewsByString.shuffle();

		List<MatchResult> trialList = new ArrayList<>();
		for (int i = 0; i < shuffle.size() - 1; i += 2) {
			MatchResult matchResult = new MatchResult();
			matchResult.setMission(mission);
			matchResult.setOwner(shuffle.get(i));
			if (shuffle.size() - 1 != i) {
				matchResult.getPairMembers().add(shuffle.get(i + 1));
			}
			trialList.add(matchResult);
		}

		if (shuffle.size() % 2 == 1) {
			trialList.get(trialList.size() - 1).getPairMembers().add(shuffle.get(shuffle.size() - 1));
		}

		boolean matched = isMatchedBefore(matchResults, trialList);
		if (matched) {
			match(crewsByString, mission, trial++);
		} else if (!matched) {
			matchResults.addAll(trialList);
		}
	}

	private boolean isMatchedBefore(List<MatchResult> matchResults, List<MatchResult> trialList) {
		AtomicBoolean result = new AtomicBoolean(false);
		for (MatchResult trial : trialList) {
			matchResults.forEach(e -> {
				if (e.getLevel().equals(trial.getLevel())
				&& !e.getOwner().equals(trial.getOwner())
				&& e.getPairMembers().contains(trial.getOwner())) {
					result.set(true);
				}
			});
		}
		return result.get();
	}

	public String getMatchResultByMission(Mission mission) {
		StringBuilder resultBuilder = new StringBuilder();
		List<MatchResult> collect = matchResults.stream().filter(match -> {
			return match.getMission().getLevel().equals(mission.getLevel())
				&& match.getMission().getName().equals(mission.getName());
		}).collect(Collectors.toList());

		collect.forEach(e -> resultBuilder.append(e.getPairMemberNames()));
		return resultBuilder.toString();
	}
}
