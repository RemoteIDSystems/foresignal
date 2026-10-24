// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'enable_free_sources_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EnableFreeSourcesResultCWProxy {
  EnableFreeSourcesResult enabled(List<String> enabled);

  EnableFreeSourcesResult count(int count);

  EnableFreeSourcesResult skipped(int skipped);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EnableFreeSourcesResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EnableFreeSourcesResult(...).copyWith(id: 12, name: "My name")
  /// ```
  EnableFreeSourcesResult call({List<String> enabled, int count, int skipped});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEnableFreeSourcesResult.copyWith(...)` or call `instanceOfEnableFreeSourcesResult.copyWith.fieldName(value)` for a single field.
class _$EnableFreeSourcesResultCWProxyImpl
    implements _$EnableFreeSourcesResultCWProxy {
  const _$EnableFreeSourcesResultCWProxyImpl(this._value);

  final EnableFreeSourcesResult _value;

  @override
  EnableFreeSourcesResult enabled(List<String> enabled) =>
      call(enabled: enabled);

  @override
  EnableFreeSourcesResult count(int count) => call(count: count);

  @override
  EnableFreeSourcesResult skipped(int skipped) => call(skipped: skipped);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EnableFreeSourcesResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EnableFreeSourcesResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EnableFreeSourcesResult call({
    Object? enabled = const $CopyWithPlaceholder(),
    Object? count = const $CopyWithPlaceholder(),
    Object? skipped = const $CopyWithPlaceholder(),
  }) {
    return EnableFreeSourcesResult(
      enabled: enabled == const $CopyWithPlaceholder() || enabled == null
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as List<String>,
      count: count == const $CopyWithPlaceholder() || count == null
          ? _value.count
          // ignore: cast_nullable_to_non_nullable
          : count as int,
      skipped: skipped == const $CopyWithPlaceholder() || skipped == null
          ? _value.skipped
          // ignore: cast_nullable_to_non_nullable
          : skipped as int,
    );
  }
}

extension $EnableFreeSourcesResultCopyWith on EnableFreeSourcesResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEnableFreeSourcesResult.copyWith(...)` or `instanceOfEnableFreeSourcesResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EnableFreeSourcesResultCWProxy get copyWith =>
      _$EnableFreeSourcesResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EnableFreeSourcesResult _$EnableFreeSourcesResultFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EnableFreeSourcesResult', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['enabled', 'count', 'skipped']);
  final val = EnableFreeSourcesResult(
    enabled: $checkedConvert(
      'enabled',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
    count: $checkedConvert('count', (v) => (v as num).toInt()),
    skipped: $checkedConvert('skipped', (v) => (v as num).toInt()),
  );
  return val;
});

Map<String, dynamic> _$EnableFreeSourcesResultToJson(
  EnableFreeSourcesResult instance,
) => <String, dynamic>{
  'enabled': instance.enabled,
  'count': instance.count,
  'skipped': instance.skipped,
};
