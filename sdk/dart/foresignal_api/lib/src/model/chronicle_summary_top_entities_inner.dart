//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_summary_top_entities_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleSummaryTopEntitiesInner {
  /// Returns a new [ChronicleSummaryTopEntitiesInner] instance.
  ChronicleSummaryTopEntitiesInner({

    required  this.ref,

     this.title,

    required  this.count,
  });

      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'ref',
    required: true,
    includeIfNull: false,
  )


  final String ref;



  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



  @JsonKey(
    
    name: r'count',
    required: true,
    includeIfNull: false,
  )


  final int count;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleSummaryTopEntitiesInner &&
      other.ref == ref &&
      other.title == title &&
      other.count == count;

    @override
    int get hashCode =>
        ref.hashCode +
        title.hashCode +
        count.hashCode;

  factory ChronicleSummaryTopEntitiesInner.fromJson(Map<String, dynamic> json) => _$ChronicleSummaryTopEntitiesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleSummaryTopEntitiesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

