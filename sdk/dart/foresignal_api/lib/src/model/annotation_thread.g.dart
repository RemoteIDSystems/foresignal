// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'annotation_thread.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnnotationThreadCWProxy {
  AnnotationThread root(Annotation root);

  AnnotationThread replies(List<Annotation> replies);

  AnnotationThread replyCount(int replyCount);

  AnnotationThread resolved(bool resolved);

  AnnotationThread lastActivityAt(DateTime lastActivityAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnnotationThread(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnnotationThread(...).copyWith(id: 12, name: "My name")
  /// ```
  AnnotationThread call({
    Annotation root,
    List<Annotation> replies,
    int replyCount,
    bool resolved,
    DateTime lastActivityAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnnotationThread.copyWith(...)` or call `instanceOfAnnotationThread.copyWith.fieldName(value)` for a single field.
class _$AnnotationThreadCWProxyImpl implements _$AnnotationThreadCWProxy {
  const _$AnnotationThreadCWProxyImpl(this._value);

  final AnnotationThread _value;

  @override
  AnnotationThread root(Annotation root) => call(root: root);

  @override
  AnnotationThread replies(List<Annotation> replies) => call(replies: replies);

  @override
  AnnotationThread replyCount(int replyCount) => call(replyCount: replyCount);

  @override
  AnnotationThread resolved(bool resolved) => call(resolved: resolved);

  @override
  AnnotationThread lastActivityAt(DateTime lastActivityAt) =>
      call(lastActivityAt: lastActivityAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnnotationThread(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnnotationThread(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnnotationThread call({
    Object? root = const $CopyWithPlaceholder(),
    Object? replies = const $CopyWithPlaceholder(),
    Object? replyCount = const $CopyWithPlaceholder(),
    Object? resolved = const $CopyWithPlaceholder(),
    Object? lastActivityAt = const $CopyWithPlaceholder(),
  }) {
    return AnnotationThread(
      root: root == const $CopyWithPlaceholder() || root == null
          ? _value.root
          // ignore: cast_nullable_to_non_nullable
          : root as Annotation,
      replies: replies == const $CopyWithPlaceholder() || replies == null
          ? _value.replies
          // ignore: cast_nullable_to_non_nullable
          : replies as List<Annotation>,
      replyCount:
          replyCount == const $CopyWithPlaceholder() || replyCount == null
          ? _value.replyCount
          // ignore: cast_nullable_to_non_nullable
          : replyCount as int,
      resolved: resolved == const $CopyWithPlaceholder() || resolved == null
          ? _value.resolved
          // ignore: cast_nullable_to_non_nullable
          : resolved as bool,
      lastActivityAt:
          lastActivityAt == const $CopyWithPlaceholder() ||
              lastActivityAt == null
          ? _value.lastActivityAt
          // ignore: cast_nullable_to_non_nullable
          : lastActivityAt as DateTime,
    );
  }
}

extension $AnnotationThreadCopyWith on AnnotationThread {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnnotationThread.copyWith(...)` or `instanceOfAnnotationThread.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnnotationThreadCWProxy get copyWith => _$AnnotationThreadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnnotationThread _$AnnotationThreadFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AnnotationThread', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'root',
          'replies',
          'replyCount',
          'resolved',
          'lastActivityAt',
        ],
      );
      final val = AnnotationThread(
        root: $checkedConvert(
          'root',
          (v) => Annotation.fromJson(v as Map<String, dynamic>),
        ),
        replies: $checkedConvert(
          'replies',
          (v) => (v as List<dynamic>)
              .map((e) => Annotation.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        replyCount: $checkedConvert('replyCount', (v) => (v as num).toInt()),
        resolved: $checkedConvert('resolved', (v) => v as bool),
        lastActivityAt: $checkedConvert(
          'lastActivityAt',
          (v) => DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$AnnotationThreadToJson(AnnotationThread instance) =>
    <String, dynamic>{
      'root': instance.root.toJson(),
      'replies': instance.replies.map((e) => e.toJson()).toList(),
      'replyCount': instance.replyCount,
      'resolved': instance.resolved,
      'lastActivityAt': instance.lastActivityAt.toIso8601String(),
    };
