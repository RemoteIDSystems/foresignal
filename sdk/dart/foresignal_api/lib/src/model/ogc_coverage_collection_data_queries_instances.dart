//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/ogc_link.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ogc_coverage_collection_data_queries_instances.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OgcCoverageCollectionDataQueriesInstances {
  /// Returns a new [OgcCoverageCollectionDataQueriesInstances] instance.
  OgcCoverageCollectionDataQueriesInstances({

     this.link,
  });

  @JsonKey(
    
    name: r'link',
    required: false,
    includeIfNull: false,
  )


  final OgcLink? link;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OgcCoverageCollectionDataQueriesInstances &&
      other.link == link;

    @override
    int get hashCode =>
        link.hashCode;

  factory OgcCoverageCollectionDataQueriesInstances.fromJson(Map<String, dynamic> json) => _$OgcCoverageCollectionDataQueriesInstancesFromJson(json);

  Map<String, dynamic> toJson() => _$OgcCoverageCollectionDataQueriesInstancesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

