// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tag_assign_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TagAssignResultCWProxy {
  TagAssignResult tagId(String tagId);

  TagAssignResult changed(int changed);

  TagAssignResult unchanged(int unchanged);

  TagAssignResult skipped(List<TagAssignResultSkippedInner> skipped);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TagAssignResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TagAssignResult(...).copyWith(id: 12, name: "My name")
  /// ```
  TagAssignResult call({
    String tagId,
    int changed,
    int unchanged,
    List<TagAssignResultSkippedInner> skipped,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTagAssignResult.copyWith(...)` or call `instanceOfTagAssignResult.copyWith.fieldName(value)` for a single field.
class _$TagAssignResultCWProxyImpl implements _$TagAssignResultCWProxy {
  const _$TagAssignResultCWProxyImpl(this._value);

  final TagAssignResult _value;

  @override
  TagAssignResult tagId(String tagId) => call(tagId: tagId);

  @override
  TagAssignResult changed(int changed) => call(changed: changed);

  @override
  TagAssignResult unchanged(int unchanged) => call(unchanged: unchanged);

  @override
  TagAssignResult skipped(List<TagAssignResultSkippedInner> skipped) =>
      call(skipped: skipped);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TagAssignResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TagAssignResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TagAssignResult call({
    Object? tagId = const $CopyWithPlaceholder(),
    Object? changed = const $CopyWithPlaceholder(),
    Object? unchanged = const $CopyWithPlaceholder(),
    Object? skipped = const $CopyWithPlaceholder(),
  }) {
    return TagAssignResult(
      tagId: tagId == const $CopyWithPlaceholder() || tagId == null
          ? _value.tagId
          // ignore: cast_nullable_to_non_nullable
          : tagId as String,
      changed: changed == const $CopyWithPlaceholder() || changed == null
          ? _value.changed
          // ignore: cast_nullable_to_non_nullable
          : changed as int,
      unchanged: unchanged == const $CopyWithPlaceholder() || unchanged == null
          ? _value.unchanged
          // ignore: cast_nullable_to_non_nullable
          : unchanged as int,
      skipped: skipped == const $CopyWithPlaceholder() || skipped == null
          ? _value.skipped
          // ignore: cast_nullable_to_non_nullable
          : skipped as List<TagAssignResultSkippedInner>,
    );
  }
}

extension $TagAssignResultCopyWith on TagAssignResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTagAssignResult.copyWith(...)` or `instanceOfTagAssignResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TagAssignResultCWProxy get copyWith => _$TagAssignResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TagAssignResult _$TagAssignResultFromJson(Map<String, dynamic> json) =>
    $checkedCreate('TagAssignResult', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['tagId', 'changed', 'unchanged', 'skipped'],
      );
      final val = TagAssignResult(
        tagId: $checkedConvert('tagId', (v) => v as String),
        changed: $checkedConvert('changed', (v) => (v as num).toInt()),
        unchanged: $checkedConvert('unchanged', (v) => (v as num).toInt()),
        skipped: $checkedConvert(
          'skipped',
          (v) => (v as List<dynamic>)
              .map(
                (e) => TagAssignResultSkippedInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$TagAssignResultToJson(TagAssignResult instance) =>
    <String, dynamic>{
      'tagId': instance.tagId,
      'changed': instance.changed,
      'unchanged': instance.unchanged,
      'skipped': instance.skipped.map((e) => e.toJson()).toList(),
    };
