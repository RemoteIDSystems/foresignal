// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'absence_window.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AbsenceWindowCWProxy {
  AbsenceWindow window(String window);

  AbsenceWindow step(String? step);

  AbsenceWindow distinctBy(String? distinctBy);

  AbsenceWindow groupBy(String? groupBy);

  AbsenceWindow maxGroups(int? maxGroups);

  AbsenceWindow coverageMin(num? coverageMin);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AbsenceWindow(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AbsenceWindow(...).copyWith(id: 12, name: "My name")
  /// ```
  AbsenceWindow call({
    String window,
    String? step,
    String? distinctBy,
    String? groupBy,
    int? maxGroups,
    num? coverageMin,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAbsenceWindow.copyWith(...)` or call `instanceOfAbsenceWindow.copyWith.fieldName(value)` for a single field.
class _$AbsenceWindowCWProxyImpl implements _$AbsenceWindowCWProxy {
  const _$AbsenceWindowCWProxyImpl(this._value);

  final AbsenceWindow _value;

  @override
  AbsenceWindow window(String window) => call(window: window);

  @override
  AbsenceWindow step(String? step) => call(step: step);

  @override
  AbsenceWindow distinctBy(String? distinctBy) => call(distinctBy: distinctBy);

  @override
  AbsenceWindow groupBy(String? groupBy) => call(groupBy: groupBy);

  @override
  AbsenceWindow maxGroups(int? maxGroups) => call(maxGroups: maxGroups);

  @override
  AbsenceWindow coverageMin(num? coverageMin) => call(coverageMin: coverageMin);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AbsenceWindow(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AbsenceWindow(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AbsenceWindow call({
    Object? window = const $CopyWithPlaceholder(),
    Object? step = const $CopyWithPlaceholder(),
    Object? distinctBy = const $CopyWithPlaceholder(),
    Object? groupBy = const $CopyWithPlaceholder(),
    Object? maxGroups = const $CopyWithPlaceholder(),
    Object? coverageMin = const $CopyWithPlaceholder(),
  }) {
    return AbsenceWindow(
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
      coverageMin: coverageMin == const $CopyWithPlaceholder()
          ? _value.coverageMin
          // ignore: cast_nullable_to_non_nullable
          : coverageMin as num?,
    );
  }
}

extension $AbsenceWindowCopyWith on AbsenceWindow {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAbsenceWindow.copyWith(...)` or `instanceOfAbsenceWindow.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AbsenceWindowCWProxy get copyWith => _$AbsenceWindowCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AbsenceWindow _$AbsenceWindowFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AbsenceWindow', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['window']);
  final val = AbsenceWindow(
    window: $checkedConvert('window', (v) => v as String),
    step: $checkedConvert('step', (v) => v as String?),
    distinctBy: $checkedConvert('distinctBy', (v) => v as String? ?? 'entity'),
    groupBy: $checkedConvert('groupBy', (v) => v as String?),
    maxGroups: $checkedConvert('maxGroups', (v) => (v as num?)?.toInt() ?? 500),
    coverageMin: $checkedConvert('coverageMin', (v) => v as num? ?? 0.8),
  );
  return val;
});

Map<String, dynamic> _$AbsenceWindowToJson(AbsenceWindow instance) =>
    <String, dynamic>{
      'window': instance.window,
      'step': ?instance.step,
      'distinctBy': ?instance.distinctBy,
      'groupBy': ?instance.groupBy,
      'maxGroups': ?instance.maxGroups,
      'coverageMin': ?instance.coverageMin,
    };
