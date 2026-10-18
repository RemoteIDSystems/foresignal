// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'comment_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CommentEventCWProxy {
  CommentEvent reason(CommentEventReasonEnum reason);

  CommentEvent annotationId(String annotationId);

  CommentEvent parentId(String? parentId);

  CommentEvent targetRef(String targetRef);

  CommentEvent author(PrincipalSummary author);

  CommentEvent excerpt(String? excerpt);

  CommentEvent at(DateTime at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CommentEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CommentEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  CommentEvent call({
    CommentEventReasonEnum reason,
    String annotationId,
    String? parentId,
    String targetRef,
    PrincipalSummary author,
    String? excerpt,
    DateTime at,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCommentEvent.copyWith(...)` or call `instanceOfCommentEvent.copyWith.fieldName(value)` for a single field.
class _$CommentEventCWProxyImpl implements _$CommentEventCWProxy {
  const _$CommentEventCWProxyImpl(this._value);

  final CommentEvent _value;

  @override
  CommentEvent reason(CommentEventReasonEnum reason) => call(reason: reason);

  @override
  CommentEvent annotationId(String annotationId) =>
      call(annotationId: annotationId);

  @override
  CommentEvent parentId(String? parentId) => call(parentId: parentId);

  @override
  CommentEvent targetRef(String targetRef) => call(targetRef: targetRef);

  @override
  CommentEvent author(PrincipalSummary author) => call(author: author);

  @override
  CommentEvent excerpt(String? excerpt) => call(excerpt: excerpt);

  @override
  CommentEvent at(DateTime at) => call(at: at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CommentEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CommentEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CommentEvent call({
    Object? reason = const $CopyWithPlaceholder(),
    Object? annotationId = const $CopyWithPlaceholder(),
    Object? parentId = const $CopyWithPlaceholder(),
    Object? targetRef = const $CopyWithPlaceholder(),
    Object? author = const $CopyWithPlaceholder(),
    Object? excerpt = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
  }) {
    return CommentEvent(
      reason: reason == const $CopyWithPlaceholder() || reason == null
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as CommentEventReasonEnum,
      annotationId:
          annotationId == const $CopyWithPlaceholder() || annotationId == null
          ? _value.annotationId
          // ignore: cast_nullable_to_non_nullable
          : annotationId as String,
      parentId: parentId == const $CopyWithPlaceholder()
          ? _value.parentId
          // ignore: cast_nullable_to_non_nullable
          : parentId as String?,
      targetRef: targetRef == const $CopyWithPlaceholder() || targetRef == null
          ? _value.targetRef
          // ignore: cast_nullable_to_non_nullable
          : targetRef as String,
      author: author == const $CopyWithPlaceholder() || author == null
          ? _value.author
          // ignore: cast_nullable_to_non_nullable
          : author as PrincipalSummary,
      excerpt: excerpt == const $CopyWithPlaceholder()
          ? _value.excerpt
          // ignore: cast_nullable_to_non_nullable
          : excerpt as String?,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
    );
  }
}

extension $CommentEventCopyWith on CommentEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCommentEvent.copyWith(...)` or `instanceOfCommentEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CommentEventCWProxy get copyWith => _$CommentEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CommentEvent _$CommentEventFromJson(Map<String, dynamic> json) =>
    $checkedCreate('CommentEvent', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'reason',
          'annotationId',
          'targetRef',
          'author',
          'at',
        ],
      );
      final val = CommentEvent(
        reason: $checkedConvert(
          'reason',
          (v) => $enumDecode(_$CommentEventReasonEnumEnumMap, v),
        ),
        annotationId: $checkedConvert('annotationId', (v) => v as String),
        parentId: $checkedConvert('parentId', (v) => v as String?),
        targetRef: $checkedConvert('targetRef', (v) => v as String),
        author: $checkedConvert(
          'author',
          (v) => PrincipalSummary.fromJson(v as Map<String, dynamic>),
        ),
        excerpt: $checkedConvert('excerpt', (v) => v as String?),
        at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
      );
      return val;
    });

Map<String, dynamic> _$CommentEventToJson(CommentEvent instance) =>
    <String, dynamic>{
      'reason': _$CommentEventReasonEnumEnumMap[instance.reason]!,
      'annotationId': instance.annotationId,
      'parentId': ?instance.parentId,
      'targetRef': instance.targetRef,
      'author': instance.author.toJson(),
      'excerpt': ?instance.excerpt,
      'at': instance.at.toIso8601String(),
    };

const _$CommentEventReasonEnumEnumMap = {
  CommentEventReasonEnum.mention: 'mention',
  CommentEventReasonEnum.reply: 'reply',
};
