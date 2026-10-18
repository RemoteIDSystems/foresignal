// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'count_window.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CountWindowCWProxy {
  CountWindow window(String window);

  CountWindow step(String? step);

  CountWindow distinctBy(String? distinctBy);

  CountWindow groupBy(String? groupBy);

  CountWindow maxGroups(int? maxGroups);

  CountWindow op(CountWindowOpEnum op);

  CountWindow threshold(int threshold);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CountWindow(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CountWindow(...).copyWith(id: 12, name: "My name")
  /// ```
  CountWindow call({
    String window,
    String? step,
    String? distinctBy,
    String? groupBy,
    int? maxGroups,
    CountWindowOpEnum op,
    int threshold,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCountWindow.copyWith(...)` or call `instanceOfCountWindow.copyWith.fieldName(value)` for a single field.
class _$CountWindowCWProxyImpl implements _$CountWindowCWProxy {
  const _$CountWindowCWProxyImpl(this._value);

  final CountWindow _value;

  @override
  CountWindow window(String window) => call(window: window);

  @override
  CountWindow step(String? step) => call(step: step);

  @override
  CountWindow distinctBy(String? distinctBy) => call(distinctBy: distinctBy);

  @override
  CountWindow groupBy(String? groupBy) => call(groupBy: groupBy);

  @override
  CountWindow maxGroups(int? maxGroups) => call(maxGroups: maxGroups);

  @override
  CountWindow op(CountWindowOpEnum op) => call(op: op);

  @override
  CountWindow threshold(int threshold) => call(threshold: threshold);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CountWindow(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CountWindow(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CountWindow call({
    Object? window = const $CopyWithPlaceholder(),
    Object? step = const $CopyWithPlaceholder(),
    Object? distinctBy = const $CopyWithPlaceholder(),
    Object? groupBy = const $CopyWithPlaceholder(),
    Object? maxGroups = const $CopyWithPlaceholder(),
    Object? op = const $CopyWithPlaceholder(),
    Object? threshold = const $CopyWithPlaceholder(),
  }) {
    return CountWindow(
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
      op: op == const $CopyWithPlaceholder() || op == null
          ? _value.op
          // ignore: cast_nullable_to_non_nullable
          : op as CountWindowOpEnum,
      threshold: threshold == const $CopyWithPlaceholder() || threshold == null
          ? _value.threshold
          // ignore: cast_nullable_to_non_nullable
          : threshold as int,
    );
  }
}

extension $CountWindowCopyWith on CountWindow {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCountWindow.copyWith(...)` or `instanceOfCountWindow.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CountWindowCWProxy get copyWith => _$CountWindowCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CountWindow _$CountWindowFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('CountWindow', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['window', 'op', 'threshold']);
  final val = CountWindow(
    window: $checkedConvert('window', (v) => v as String),
    step: $checkedConvert('step', (v) => v as String?),
    distinctBy: $checkedConvert('distinctBy', (v) => v as String? ?? 'entity'),
    groupBy: $checkedConvert('groupBy', (v) => v as String?),
    maxGroups: $checkedConvert('maxGroups', (v) => (v as num?)?.toInt() ?? 500),
    op: $checkedConvert(
      'op',
      (v) => $enumDecode(_$CountWindowOpEnumEnumMap, v),
    ),
    threshold: $checkedConvert('threshold', (v) => (v as num).toInt()),
  );
  return val;
});

Map<String, dynamic> _$CountWindowToJson(CountWindow instance) =>
    <String, dynamic>{
      'window': instance.window,
      'step': ?instance.step,
      'distinctBy': ?instance.distinctBy,
      'groupBy': ?instance.groupBy,
      'maxGroups': ?instance.maxGroups,
      'op': _$CountWindowOpEnumEnumMap[instance.op]!,
      'threshold': instance.threshold,
    };

const _$CountWindowOpEnumEnumMap = {
  CountWindowOpEnum.greaterThanEqual: '>=',
  CountWindowOpEnum.lessThanEqual: '<=',
};
