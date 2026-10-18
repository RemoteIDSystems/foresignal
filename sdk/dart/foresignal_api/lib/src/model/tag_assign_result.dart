//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/tag_assign_result_skipped_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'tag_assign_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TagAssignResult {
  /// Returns a new [TagAssignResult] instance.
  TagAssignResult({

    required  this.tagId,

    required  this.changed,

    required  this.unchanged,

    required  this.skipped,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'tagId',
    required: true,
    includeIfNull: false,
  )


  final String tagId;



      /// Assignments created (assign) or removed (unassign)
  @JsonKey(
    
    name: r'changed',
    required: true,
    includeIfNull: false,
  )


  final int changed;



      /// Already assigned (assign) or not assigned (unassign)
  @JsonKey(
    
    name: r'unchanged',
    required: true,
    includeIfNull: false,
  )


  final int unchanged;



  @JsonKey(
    
    name: r'skipped',
    required: true,
    includeIfNull: false,
  )


  final List<TagAssignResultSkippedInner> skipped;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TagAssignResult &&
      other.tagId == tagId &&
      other.changed == changed &&
      other.unchanged == unchanged &&
      other.skipped == skipped;

    @override
    int get hashCode =>
        tagId.hashCode +
        changed.hashCode +
        unchanged.hashCode +
        skipped.hashCode;

  factory TagAssignResult.fromJson(Map<String, dynamic> json) => _$TagAssignResultFromJson(json);

  Map<String, dynamic> toJson() => _$TagAssignResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

