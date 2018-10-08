package objective.taskboard.cluster;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import objective.taskboard.followup.cluster.SizingCluster;
import objective.taskboard.followup.cluster.SizingClusterRepository;

@Repository
public class ClusterRepositoryImpl implements ClusterRepository {

    private final SizingClusterRepository repository;

    @Autowired
    public ClusterRepositoryImpl(SizingClusterRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<SizingCluster> findById(Long id) {
        return Optional.ofNullable(repository.findOne(id));
    }

    @Override
    public List<SizingCluster> findAll() {
        return repository.findAll();
    }

    @Override
    public SizingCluster save(SizingCluster cluster) {
        return repository.save(cluster);
    }

}
