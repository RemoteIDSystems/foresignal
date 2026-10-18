//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'narrative_merge_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NarrativeMergeInput {
  /// Returns a new [NarrativeMergeInput] instance.
  NarrativeMergeInput({

    required  this.narrativeIds,

     this.reason,
  });

  @JsonKey(
    
    name: r'narrativeIds',
    required: true,
    includeIfNull: false,
  )


  final List<String> narrativeIds;



  @JsonKey(
    
    name: r'reason',
    required: false,
    includeIfNull: false,
  )


  final String? reason;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NarrativeMergeInput &&
      other.narrativeIds == narrativeIds &&
      other.reason == reason;

    @override
    int get hashCode =>
        narrativeIds.hashCode +
        (reason == null ? 0 : reason.hashCode);

  factory NarrativeMergeInput.fromJson(Map<String, dynamic> json) => _$NarrativeMergeInputFromJson(json);

  Map<String, dynamic> toJson() => _$NarrativeMergeInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

