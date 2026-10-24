// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'intel_corroboration_auto_corroborate.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IntelCorroborationAutoCorroborateCWProxy {
  IntelCorroborationAutoCorroborate enabled(bool? enabled);

  IntelCorroborationAutoCorroborate withinKm(num? withinKm);

  IntelCorroborationAutoCorroborate window(String? window);

  IntelCorroborationAutoCorroborate maxMembers(int? maxMembers);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelCorroborationAutoCorroborate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelCorroborationAutoCorroborate(...).copyWith(id: 12, name: "My name")
  /// ```
  IntelCorroborationAutoCorroborate call({
    bool? enabled,
    num? withinKm,
    String? window,
    int? maxMembers,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIntelCorroborationAutoCorroborate.copyWith(...)` or call `instanceOfIntelCorroborationAutoCorroborate.copyWith.fieldName(value)` for a single field.
class _$IntelCorroborationAutoCorroborateCWProxyImpl
    implements _$IntelCorroborationAutoCorroborateCWProxy {
  const _$IntelCorroborationAutoCorroborateCWProxyImpl(this._value);

  final IntelCorroborationAutoCorroborate _value;

  @override
  IntelCorroborationAutoCorroborate enabled(bool? enabled) =>
      call(enabled: enabled);

  @override
  IntelCorroborationAutoCorroborate withinKm(num? withinKm) =>
      call(withinKm: withinKm);

  @override
  IntelCorroborationAutoCorroborate window(String? window) =>
      call(window: window);

  @override
  IntelCorroborationAutoCorroborate maxMembers(int? maxMembers) =>
      call(maxMembers: maxMembers);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelCorroborationAutoCorroborate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelCorroborationAutoCorroborate(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IntelCorroborationAutoCorroborate call({
    Object? enabled = const $CopyWithPlaceholder(),
    Object? withinKm = const $CopyWithPlaceholder(),
    Object? window = const $CopyWithPlaceholder(),
    Object? maxMembers = const $CopyWithPlaceholder(),
  }) {
    return IntelCorroborationAutoCorroborate(
      enabled: enabled == const $CopyWithPlaceholder()
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool?,
      withinKm: withinKm == const $CopyWithPlaceholder()
          ? _value.withinKm
          // ignore: cast_nullable_to_non_nullable
          : withinKm as num?,
      window: window == const $CopyWithPlaceholder()
          ? _value.window
          // ignore: cast_nullable_to_non_nullable
          : window as String?,
      maxMembers: maxMembers == const $CopyWithPlaceholder()
          ? _value.maxMembers
          // ignore: cast_nullable_to_non_nullable
          : maxMembers as int?,
    );
  }
}

extension $IntelCorroborationAutoCorroborateCopyWith
    on IntelCorroborationAutoCorroborate {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIntelCorroborationAutoCorroborate.copyWith(...)` or `instanceOfIntelCorroborationAutoCorroborate.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IntelCorroborationAutoCorroborateCWProxy get copyWith =>
      _$IntelCorroborationAutoCorroborateCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IntelCorroborationAutoCorroborate _$IntelCorroborationAutoCorroborateFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IntelCorroborationAutoCorroborate', json, (
  $checkedConvert,
) {
  final val = IntelCorroborationAutoCorroborate(
    enabled: $checkedConvert('enabled', (v) => v as bool?),
    withinKm: $checkedConvert('withinKm', (v) => v as num?),
    window: $checkedConvert('window', (v) => v as String?),
    maxMembers: $checkedConvert('maxMembers', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$IntelCorroborationAutoCorroborateToJson(
  IntelCorroborationAutoCorroborate instance,
) => <String, dynamic>{
  'enabled': ?instance.enabled,
  'withinKm': ?instance.withinKm,
  'window': ?instance.window,
  'maxMembers': ?instance.maxMembers,
};
