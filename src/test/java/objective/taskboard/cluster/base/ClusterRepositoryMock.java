package objective.taskboard.cluster.base;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import objective.taskboard.cluster.ClusterRepository;
import objective.taskboard.followup.cluster.SizingCluster;

public class ClusterRepositoryMock implements ClusterRepository {

    private static final Map<Long, SizingCluster> data = new HashMap<>();
    private static long id;

    @Override
    public Optional<SizingCluster> findById(Long cluster) {
        return ofNullable(data.get(cluster));
    }

    @Override
    public SizingCluster save(SizingCluster cluster) {
        if (cluster.getId() == null) {
            cluster.setId(++id);
            data.put(cluster.getId(), cluster);
        } else {
            data.put(cluster.getId(), cluster);
        }
        return cluster;
    }

    @Override
    public List<SizingCluster> findAll() {
        return data.values().stream().collect(toList());
    }

    public void reset() {
        data.clear();
        id = 0;
    }
}
