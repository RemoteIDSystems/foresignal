//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/ogc_coverage_collection_data_queries_area.dart';
import 'package:foresignal_api/src/model/ogc_coverage_collection_data_queries_position.dart';
import 'package:foresignal_api/src/model/ogc_coverage_collection_data_queries_instances.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ogc_coverage_collection_data_queries.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OgcCoverageCollectionDataQueries {
  /// Returns a new [OgcCoverageCollectionDataQueries] instance.
  OgcCoverageCollectionDataQueries({

     this.position,

     this.area,

     this.instances,
  });

  @JsonKey(
    
    name: r'position',
    required: false,
    includeIfNull: false,
  )


  final OgcCoverageCollectionDataQueriesPosition? position;



  @JsonKey(
    
    name: r'area',
    required: false,
    includeIfNull: false,
  )


  final OgcCoverageCollectionDataQueriesArea? area;



  @JsonKey(
    
    name: r'instances',
    required: false,
    includeIfNull: false,
  )


  final OgcCoverageCollectionDataQueriesInstances? instances;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OgcCoverageCollectionDataQueries &&
      other.position == position &&
      other.area == area &&
      other.instances == instances;

    @override
    int get hashCode =>
        position.hashCode +
        area.hashCode +
        instances.hashCode;

  factory OgcCoverageCollectionDataQueries.fromJson(Map<String, dynamic> json) => _$OgcCoverageCollectionDataQueriesFromJson(json);

  Map<String, dynamic> toJson() => _$OgcCoverageCollectionDataQueriesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

