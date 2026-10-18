// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'window_spec_common.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$WindowSpecCommonCWProxy {
  WindowSpecCommon window(String window);

  WindowSpecCommon step(String? step);

  WindowSpecCommon distinctBy(String? distinctBy);

  WindowSpecCommon groupBy(String? groupBy);

  WindowSpecCommon maxGroups(int? maxGroups);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WindowSpecCommon(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WindowSpecCommon(...).copyWith(id: 12, name: "My name")
  /// ```
  WindowSpecCommon call({
    String window,
    String? step,
    String? distinctBy,
    String? groupBy,
    int? maxGroups,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfWindowSpecCommon.copyWith(...)` or call `instanceOfWindowSpecCommon.copyWith.fieldName(value)` for a single field.
class _$WindowSpecCommonCWProxyImpl implements _$WindowSpecCommonCWProxy {
  const _$WindowSpecCommonCWProxyImpl(this._value);

  final WindowSpecCommon _value;

  @override
  WindowSpecCommon window(String window) => call(window: window);

  @override
  WindowSpecCommon step(String? step) => call(step: step);

  @override
  WindowSpecCommon distinctBy(String? distinctBy) =>
      call(distinctBy: distinctBy);

  @override
  WindowSpecCommon groupBy(String? groupBy) => call(groupBy: groupBy);

  @override
  WindowSpecCommon maxGroups(int? maxGroups) => call(maxGroups: maxGroups);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WindowSpecCommon(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WindowSpecCommon(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  WindowSpecCommon call({
    Object? window = const $CopyWithPlaceholder(),
    Object? step = const $CopyWithPlaceholder(),
    Object? distinctBy = const $CopyWithPlaceholder(),
    Object? groupBy = const $CopyWithPlaceholder(),
    Object? maxGroups = const $CopyWithPlaceholder(),
  }) {
    return WindowSpecCommon(
      window: window == const $CopyWithPlaceholder() || window == null
          ? _value.window
          // ignore: cast_nullable_to_non_nullable
          : window as String,
      step: step == const $CopyWithPlaceholder()
          ? _value.step
          // ignore: cast_nullable_to_non_nullable
          : step as String?,
      distinctBy: distinctBy == const $CopyWithPlaceholder()
          ? _value.distinctBy
          // ignore: cast_nullable_to_non_nullable
          : distinctBy as String?,
      groupBy: groupBy == const $CopyWithPlaceholder()
          ? _value.groupBy
          // ignore: cast_nullable_to_non_nullable
          : groupBy as String?,
      maxGroups: maxGroups == const $CopyWithPlaceholder()
          ? _value.maxGroups
          // ignore: cast_nullable_to_non_nullable
          : maxGroups as int?,
    );
  }
}

extension $WindowSpecCommonCopyWith on WindowSpecCommon {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfWindowSpecCommon.copyWith(...)` or `instanceOfWindowSpecCommon.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$WindowSpecCommonCWProxy get copyWith => _$WindowSpecCommonCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

WindowSpecCommon _$WindowSpecCommonFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('WindowSpecCommon', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['window']);
  final val = WindowSpecCommon(
    window: $checkedConvert('window', (v) => v as String),
    step: $checkedConvert('step', (v) => v as String?),
    distinctBy: $checkedConvert('distinctBy', (v) => v as String? ?? 'entity'),
    groupBy: $checkedConvert('groupBy', (v) => v as String?),
    maxGroups: $checkedConvert('maxGroups', (v) => (v as num?)?.toInt() ?? 500),
  );
  return val;
});

Map<String, dynamic> _$WindowSpecCommonToJson(WindowSpecCommon instance) =>
    <String, dynamic>{
      'window': instance.window,
      'step': ?instance.step,
      'distinctBy': ?instance.distinctBy,
      'groupBy': ?instance.groupBy,
      'maxGroups': ?instance.maxGroups,
    };
