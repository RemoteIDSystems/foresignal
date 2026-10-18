//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'entity_merge_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EntityMergeInput {
  /// Returns a new [EntityMergeInput] instance.
  EntityMergeInput({

    required  this.otherId,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'otherId',
    required: true,
    includeIfNull: false,
  )


  final String otherId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EntityMergeInput &&
      other.otherId == otherId;

    @override
    int get hashCode =>
        otherId.hashCode;

  factory EntityMergeInput.fromJson(Map<String, dynamic> json) => _$EntityMergeInputFromJson(json);

  Map<String, dynamic> toJson() => _$EntityMergeInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

