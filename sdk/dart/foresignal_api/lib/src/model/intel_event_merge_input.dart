//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'intel_event_merge_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IntelEventMergeInput {
  /// Returns a new [IntelEventMergeInput] instance.
  IntelEventMergeInput({

    required  this.intoId,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'intoId',
    required: true,
    includeIfNull: false,
  )


  final String intoId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IntelEventMergeInput &&
      other.intoId == intoId;

    @override
    int get hashCode =>
        intoId.hashCode;

  factory IntelEventMergeInput.fromJson(Map<String, dynamic> json) => _$IntelEventMergeInputFromJson(json);

  Map<String, dynamic> toJson() => _$IntelEventMergeInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

