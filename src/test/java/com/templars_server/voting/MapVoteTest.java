package com.templars_server.voting;

import com.templars_server.model.Context;
import com.templars_server.model.GameMap;
import com.templars_server.model.GameMapList;
import com.templars_server.model.Player;
import com.templars_server.util.rcon.RconClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class MapVoteTest {

    private static final int TEST_PLAYERLIST_SIZE = 32;
    private static final int TEST_MAP_LIST_SIZE = 32;
    private static final int TEST_DEFAULT_COOLDOWN = 1;
    private static final int TEST_DEFAULT_MAXROUNDS = 20;
    private static Context context;

    @BeforeEach
    void setup() {
        GameMapList mapList = new GameMapList();
        for (int i = 1; i<= TEST_MAP_LIST_SIZE; i++) {
            String name = "test_map_" + i;
            mapList.put(name, new GameMap(name, TEST_DEFAULT_MAXROUNDS));
        }

        context = new Context(
                mock(RconClient.class),
                mapList,
                TEST_DEFAULT_COOLDOWN,
                null,
                null,
                false,
                false,
                false,
                null
        );
        for (int i=1; i<=TEST_PLAYERLIST_SIZE; i++) {
            context.getPlayers().put(i, new Player(i, "test_player_" + i));
        }
    }

    @Test
    void testCollectChoices_EmptyMapList_OnlyDontChangeOption() {
        context.getMaps().clear();
        List<String> choices = MapVote.collectChoices(context);

        assertThat(choices).containsOnly(Vote.DONT_CHANGE);
    }

    @Test
    void testCollectChoices_NoNominations_FiveRandomMapsAndDontChange() {
        List<String> choices = MapVote.collectChoices(context);

        assertThat(choices).hasSize(Vote.MAX_CHOICES);
        assertThat(choices).containsAnyOf(context.getMaps().keySet().toArray(new String[]{}));
        assertThat(choices).doesNotHaveDuplicates();
        assertThat(choices).containsOnlyOnce(Vote.DONT_CHANGE);
    }

    @Test
    void testCollectChoices_OneNomination_NominationOccursInList() {
        String testMapName = "test_map_1";
        context.getPlayers().get(1).setNomination(testMapName);
        List<String> choices = MapVote.collectChoices(context);

        assertThat(choices).hasSize(Vote.MAX_CHOICES);
        assertThat(choices).containsAnyOf(context.getMaps().keySet().toArray(new String[]{}));
        assertThat(choices).doesNotHaveDuplicates();
        assertThat(choices).containsOnlyOnce(testMapName);
        assertThat(choices).containsOnlyOnce(Vote.DONT_CHANGE);
    }

    @Test
    void testCollectChoices_OneNominationOneMapInMaplist_NoDuplicates() {
        String testMapName = "test_map_1";
        context.getPlayers().get(1).setNomination(testMapName);
        context.getMaps().clear();
        context.getMaps().put(testMapName, new GameMap(testMapName, TEST_DEFAULT_MAXROUNDS));
        List<String> choices = MapVote.collectChoices(context);

        assertThat(choices).hasSize(2);
        assertThat(choices).containsAnyOf(context.getMaps().keySet().toArray(new String[]{}));
        assertThat(choices).doesNotHaveDuplicates();
        assertThat(choices).containsOnlyOnce(testMapName);
        assertThat(choices).containsOnlyOnce(Vote.DONT_CHANGE);
    }

    @Test
    void testCollectChoices_AllNominations_RandomNominationsOccurInList() {
        for (int i=1; i<=TEST_PLAYERLIST_SIZE; i++) {
            context.getPlayers().get(i).setNomination("test_nomination_" + i);
        }
        List<String> choices = MapVote.collectChoices(context);

        assertThat(choices).hasSize(Vote.MAX_CHOICES);
        assertThat(choices).doesNotContain(context.getMaps().keySet().toArray(new String[]{}));
        assertThat(choices).doesNotHaveDuplicates();
        assertThat(choices).containsOnlyOnce(Vote.DONT_CHANGE);
    }

    @Test
    void testCollectChoices_AllSameNomination_NominationOccursOnceInList() {
        String testMapName = "test_nomination_1";
        for (int i=1; i<=TEST_PLAYERLIST_SIZE; i++) {
            context.getPlayers().get(i).setNomination(testMapName);
        }

        List<String> choices = MapVote.collectChoices(context);

        assertThat(choices).hasSize(Vote.MAX_CHOICES);
        assertThat(choices).containsAnyOf(context.getMaps().keySet().toArray(new String[]{}));
        assertThat(choices).doesNotHaveDuplicates();
        assertThat(choices).containsOnlyOnce(testMapName);
        assertThat(choices).containsOnlyOnce(Vote.DONT_CHANGE);
    }

    @Test
    void testCollectNominations_EmptyServer_EmptyList() {
        context.getPlayers().clear();
        List<String> nominations = MapVote.collectNominations(context.getPlayers());

        assertThat(nominations).isEmpty();
    }

    @Test
    void testCollectNominations_OneNomination_NominationInList() {
        String testMapName = "test_map_1";
        context.getPlayers().get(1).setNomination(testMapName);
        List<String> nominations = MapVote.collectNominations(context.getPlayers());

        assertThat(nominations).containsExactly(testMapName);
    }

    @Test
    void testCollectNominations_OneDuplicateNomination_OnlyOneNominationInList() {
        String testMapName = "test_map_1";
        context.getPlayers().get(1).setNomination(testMapName);
        context.getPlayers().get(2).setNomination(testMapName);
        List<String> nominations = MapVote.collectNominations(context.getPlayers());

        assertThat(nominations).containsExactly(testMapName);
        assertThat(nominations).doesNotHaveDuplicates();
    }

    @Test
    void testCollectNominations_SixNominations_FiveNominationsInList() {
        String[] testMaps = new String[] {
                "test_map_1",
                "test_map_2",
                "test_map_3",
                "test_map_4",
                "test_map_5",
                "test_map_6"
        };
        for (int i=0; i<testMaps.length; i++) {
            context.getPlayers().get(i+1).setNomination(testMaps[i]);
        }

        List<String> nominations = MapVote.collectNominations(context.getPlayers());

        assertThat(nominations).hasSize(5);
        assertThat(nominations).containsAnyOf(testMaps);
        assertThat(nominations).doesNotHaveDuplicates();
    }

    @Test
    void testCollectRandomMaps_EmptyMapList_EmptyList() {
        context.getMaps().clear();
        List<String> randomMaps = MapVote.collectRandomMaps(context.getMaps(), List.of(context.getCurrentMap().getName()), 1);

        assertThat(randomMaps).isEmpty();
    }

    @Test
    void testCollectRandomMaps_OneRandomMap_OneMapInList() {
        List<String> randomMaps = MapVote.collectRandomMaps(context.getMaps(), List.of(context.getCurrentMap().getName()), 1);

        assertThat(randomMaps).hasSize(1);
        assertThat(randomMaps).containsAnyOf(context.getMaps().keySet().toArray(new String[]{}));
    }

    @Test
    void testCollectRandomMaps_TooManyMaps_SameSizeAsMapList() {
        List<String> randomMaps = MapVote.collectRandomMaps(context.getMaps(), List.of(context.getCurrentMap().getName()), TEST_MAP_LIST_SIZE + 1);

        assertThat(randomMaps).hasSize(TEST_MAP_LIST_SIZE);
        assertThat(randomMaps).containsAnyOf(context.getMaps().keySet().toArray(new String[]{}));
        assertThat(randomMaps).doesNotHaveDuplicates();
    }

    @Test
    void testCollectRandomMaps_ForceCurrentMap_EmptyList() {
        String testMap = "test_map_1";
        context.getMaps().clear();
        context.getMaps().put(testMap, new GameMap(testMap, TEST_DEFAULT_MAXROUNDS));
        context.setCurrentMap(new GameMap(testMap, TEST_DEFAULT_MAXROUNDS));
        List<String> randomMaps = MapVote.collectRandomMaps(context.getMaps(), List.of(context.getCurrentMap().getName()), 1);

        assertThat(randomMaps).isEmpty();
    }

}
