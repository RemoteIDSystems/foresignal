//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/ogc_link.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ogc_coverage_collection_data_queries_area.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OgcCoverageCollectionDataQueriesArea {
  /// Returns a new [OgcCoverageCollectionDataQueriesArea] instance.
  OgcCoverageCollectionDataQueriesArea({

     this.link,
  });

  @JsonKey(
    
    name: r'link',
    required: false,
    includeIfNull: false,
  )


  final OgcLink? link;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OgcCoverageCollectionDataQueriesArea &&
      other.link == link;

    @override
    int get hashCode =>
        link.hashCode;

  factory OgcCoverageCollectionDataQueriesArea.fromJson(Map<String, dynamic> json) => _$OgcCoverageCollectionDataQueriesAreaFromJson(json);

  Map<String, dynamic> toJson() => _$OgcCoverageCollectionDataQueriesAreaToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

