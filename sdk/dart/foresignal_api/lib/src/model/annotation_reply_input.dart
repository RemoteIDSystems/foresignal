//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'annotation_reply_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnnotationReplyInput {
  /// Returns a new [AnnotationReplyInput] instance.
  AnnotationReplyInput({

    required  this.body,

     this.private = false,
  });

      /// Markdown
  @JsonKey(
    
    name: r'body',
    required: true,
    includeIfNull: false,
  )


  final String body;



  @JsonKey(
    defaultValue: false,
    name: r'private',
    required: false,
    includeIfNull: false,
  )


  final bool? private;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnnotationReplyInput &&
      other.body == body &&
      other.private == private;

    @override
    int get hashCode =>
        body.hashCode +
        private.hashCode;

  factory AnnotationReplyInput.fromJson(Map<String, dynamic> json) => _$AnnotationReplyInputFromJson(json);

  Map<String, dynamic> toJson() => _$AnnotationReplyInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

