//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/annotation.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'annotation_thread.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnnotationThread {
  /// Returns a new [AnnotationThread] instance.
  AnnotationThread({

    required  this.root,

    required  this.replies,

    required  this.replyCount,

    required  this.resolved,

    required  this.lastActivityAt,
  });

  @JsonKey(
    
    name: r'root',
    required: true,
    includeIfNull: false,
  )


  final Annotation root;



  @JsonKey(
    
    name: r'replies',
    required: true,
    includeIfNull: false,
  )


  final List<Annotation> replies;



  @JsonKey(
    
    name: r'replyCount',
    required: true,
    includeIfNull: false,
  )


  final int replyCount;



  @JsonKey(
    
    name: r'resolved',
    required: true,
    includeIfNull: false,
  )


  final bool resolved;



  @JsonKey(
    
    name: r'lastActivityAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime lastActivityAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnnotationThread &&
      other.root == root &&
      other.replies == replies &&
      other.replyCount == replyCount &&
      other.resolved == resolved &&
      other.lastActivityAt == lastActivityAt;

    @override
    int get hashCode =>
        root.hashCode +
        replies.hashCode +
        replyCount.hashCode +
        resolved.hashCode +
        lastActivityAt.hashCode;

  factory AnnotationThread.fromJson(Map<String, dynamic> json) => _$AnnotationThreadFromJson(json);

  Map<String, dynamic> toJson() => _$AnnotationThreadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

