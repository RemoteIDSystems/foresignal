//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'hypothesis_set_draft_warnings_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class HypothesisSetDraftWarningsInner {
  /// Returns a new [HypothesisSetDraftWarningsInner] instance.
  HypothesisSetDraftWarningsInner({

    required  this.code,

    required  this.message,

     this.path,
  });

  @JsonKey(
    
    name: r'code',
    required: true,
    includeIfNull: false,
  )


  final String code;



  @JsonKey(
    
    name: r'message',
    required: true,
    includeIfNull: false,
  )


  final String message;



  @JsonKey(
    
    name: r'path',
    required: false,
    includeIfNull: false,
  )


  final String? path;





    @override
    bool operator ==(Object other) => identical(this, other) || other is HypothesisSetDraftWarningsInner &&
      other.code == code &&
      other.message == message &&
      other.path == path;

    @override
    int get hashCode =>
        code.hashCode +
        message.hashCode +
        (path == null ? 0 : path.hashCode);

  factory HypothesisSetDraftWarningsInner.fromJson(Map<String, dynamic> json) => _$HypothesisSetDraftWarningsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$HypothesisSetDraftWarningsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

