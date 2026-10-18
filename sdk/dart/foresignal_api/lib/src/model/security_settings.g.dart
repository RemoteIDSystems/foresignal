// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'security_settings.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SecuritySettingsCWProxy {
  SecuritySettings openReads(bool openReads);

  SecuritySettings exposeObserver(bool? exposeObserver);

  SecuritySettings anonymous(ReadRestriction? anonymous);

  SecuritySettings metricsOpen(bool? metricsOpen);

  SecuritySettings exposeMlat(bool? exposeMlat);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SecuritySettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SecuritySettings(...).copyWith(id: 12, name: "My name")
  /// ```
  SecuritySettings call({
    bool openReads,
    bool? exposeObserver,
    ReadRestriction? anonymous,
    bool? metricsOpen,
    bool? exposeMlat,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSecuritySettings.copyWith(...)` or call `instanceOfSecuritySettings.copyWith.fieldName(value)` for a single field.
class _$SecuritySettingsCWProxyImpl implements _$SecuritySettingsCWProxy {
  const _$SecuritySettingsCWProxyImpl(this._value);

  final SecuritySettings _value;

  @override
  SecuritySettings openReads(bool openReads) => call(openReads: openReads);

  @override
  SecuritySettings exposeObserver(bool? exposeObserver) =>
      call(exposeObserver: exposeObserver);

  @override
  SecuritySettings anonymous(ReadRestriction? anonymous) =>
      call(anonymous: anonymous);

  @override
  SecuritySettings metricsOpen(bool? metricsOpen) =>
      call(metricsOpen: metricsOpen);

  @override
  SecuritySettings exposeMlat(bool? exposeMlat) => call(exposeMlat: exposeMlat);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SecuritySettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SecuritySettings(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SecuritySettings call({
    Object? openReads = const $CopyWithPlaceholder(),
    Object? exposeObserver = const $CopyWithPlaceholder(),
    Object? anonymous = const $CopyWithPlaceholder(),
    Object? metricsOpen = const $CopyWithPlaceholder(),
    Object? exposeMlat = const $CopyWithPlaceholder(),
  }) {
    return SecuritySettings(
      openReads: openReads == const $CopyWithPlaceholder() || openReads == null
          ? _value.openReads
          // ignore: cast_nullable_to_non_nullable
          : openReads as bool,
      exposeObserver: exposeObserver == const $CopyWithPlaceholder()
          ? _value.exposeObserver
          // ignore: cast_nullable_to_non_nullable
          : exposeObserver as bool?,
      anonymous: anonymous == const $CopyWithPlaceholder()
          ? _value.anonymous
          // ignore: cast_nullable_to_non_nullable
          : anonymous as ReadRestriction?,
      metricsOpen: metricsOpen == const $CopyWithPlaceholder()
          ? _value.metricsOpen
          // ignore: cast_nullable_to_non_nullable
          : metricsOpen as bool?,
      exposeMlat: exposeMlat == const $CopyWithPlaceholder()
          ? _value.exposeMlat
          // ignore: cast_nullable_to_non_nullable
          : exposeMlat as bool?,
    );
  }
}

extension $SecuritySettingsCopyWith on SecuritySettings {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSecuritySettings.copyWith(...)` or `instanceOfSecuritySettings.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SecuritySettingsCWProxy get copyWith => _$SecuritySettingsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SecuritySettings _$SecuritySettingsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SecuritySettings', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['openReads']);
      final val = SecuritySettings(
        openReads: $checkedConvert('openReads', (v) => v as bool? ?? true),
        exposeObserver: $checkedConvert(
          'exposeObserver',
          (v) => v as bool? ?? false,
        ),
        anonymous: $checkedConvert(
          'anonymous',
          (v) => v == null
              ? null
              : ReadRestriction.fromJson(v as Map<String, dynamic>),
        ),
        metricsOpen: $checkedConvert('metricsOpen', (v) => v as bool? ?? true),
        exposeMlat: $checkedConvert('exposeMlat', (v) => v as bool? ?? false),
      );
      return val;
    });

Map<String, dynamic> _$SecuritySettingsToJson(SecuritySettings instance) =>
    <String, dynamic>{
      'openReads': instance.openReads,
      'exposeObserver': ?instance.exposeObserver,
      'anonymous': ?instance.anonymous?.toJson(),
      'metricsOpen': ?instance.metricsOpen,
      'exposeMlat': ?instance.exposeMlat,
    };
