//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'annotation_resolve_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnnotationResolveInput {
  /// Returns a new [AnnotationResolveInput] instance.
  AnnotationResolveInput({

     this.resolved = true,
  });

  @JsonKey(
    defaultValue: true,
    name: r'resolved',
    required: false,
    includeIfNull: false,
  )


  final bool? resolved;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnnotationResolveInput &&
      other.resolved == resolved;

    @override
    int get hashCode =>
        resolved.hashCode;

  factory AnnotationResolveInput.fromJson(Map<String, dynamic> json) => _$AnnotationResolveInputFromJson(json);

  Map<String, dynamic> toJson() => _$AnnotationResolveInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

