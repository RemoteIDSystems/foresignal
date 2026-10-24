// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'enable_core_sources_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EnableCoreSourcesResultCWProxy {
  EnableCoreSourcesResult enabled(List<String> enabled);

  EnableCoreSourcesResult count(int count);

  EnableCoreSourcesResult skipped(int skipped);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EnableCoreSourcesResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EnableCoreSourcesResult(...).copyWith(id: 12, name: "My name")
  /// ```
  EnableCoreSourcesResult call({List<String> enabled, int count, int skipped});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEnableCoreSourcesResult.copyWith(...)` or call `instanceOfEnableCoreSourcesResult.copyWith.fieldName(value)` for a single field.
class _$EnableCoreSourcesResultCWProxyImpl
    implements _$EnableCoreSourcesResultCWProxy {
  const _$EnableCoreSourcesResultCWProxyImpl(this._value);

  final EnableCoreSourcesResult _value;

  @override
  EnableCoreSourcesResult enabled(List<String> enabled) =>
      call(enabled: enabled);

  @override
  EnableCoreSourcesResult count(int count) => call(count: count);

  @override
  EnableCoreSourcesResult skipped(int skipped) => call(skipped: skipped);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EnableCoreSourcesResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EnableCoreSourcesResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EnableCoreSourcesResult call({
    Object? enabled = const $CopyWithPlaceholder(),
    Object? count = const $CopyWithPlaceholder(),
    Object? skipped = const $CopyWithPlaceholder(),
  }) {
    return EnableCoreSourcesResult(
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

extension $EnableCoreSourcesResultCopyWith on EnableCoreSourcesResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEnableCoreSourcesResult.copyWith(...)` or `instanceOfEnableCoreSourcesResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EnableCoreSourcesResultCWProxy get copyWith =>
      _$EnableCoreSourcesResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EnableCoreSourcesResult _$EnableCoreSourcesResultFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EnableCoreSourcesResult', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['enabled', 'count', 'skipped']);
  final val = EnableCoreSourcesResult(
    enabled: $checkedConvert(
      'enabled',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
    count: $checkedConvert('count', (v) => (v as num).toInt()),
    skipped: $checkedConvert('skipped', (v) => (v as num).toInt()),
  );
  return val;
});

Map<String, dynamic> _$EnableCoreSourcesResultToJson(
  EnableCoreSourcesResult instance,
) => <String, dynamic>{
  'enabled': instance.enabled,
  'count': instance.count,
  'skipped': instance.skipped,
};
