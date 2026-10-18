//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'situation_merge_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SituationMergeInput {
  /// Returns a new [SituationMergeInput] instance.
  SituationMergeInput({

    required  this.intoId,

     this.version,

     this.reason,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'intoId',
    required: true,
    includeIfNull: false,
  )


  final String intoId;



      /// Expected version of the path situation
  @JsonKey(
    
    name: r'version',
    required: false,
    includeIfNull: false,
  )


  final int? version;



  @JsonKey(
    
    name: r'reason',
    required: false,
    includeIfNull: false,
  )


  final String? reason;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SituationMergeInput &&
      other.intoId == intoId &&
      other.version == version &&
      other.reason == reason;

    @override
    int get hashCode =>
        intoId.hashCode +
        version.hashCode +
        (reason == null ? 0 : reason.hashCode);

  factory SituationMergeInput.fromJson(Map<String, dynamic> json) => _$SituationMergeInputFromJson(json);

  Map<String, dynamic> toJson() => _$SituationMergeInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

