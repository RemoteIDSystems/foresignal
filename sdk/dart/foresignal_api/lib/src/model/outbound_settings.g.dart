// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'outbound_settings.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OutboundSettingsCWProxy {
  OutboundSettings allowHosts(List<String>? allowHosts);

  OutboundSettings allowCidrs(List<String>? allowCidrs);

  OutboundSettings denyHosts(List<String>? denyHosts);

  OutboundSettings denyCidrs(List<String>? denyCidrs);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OutboundSettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OutboundSettings(...).copyWith(id: 12, name: "My name")
  /// ```
  OutboundSettings call({
    List<String>? allowHosts,
    List<String>? allowCidrs,
    List<String>? denyHosts,
    List<String>? denyCidrs,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOutboundSettings.copyWith(...)` or call `instanceOfOutboundSettings.copyWith.fieldName(value)` for a single field.
class _$OutboundSettingsCWProxyImpl implements _$OutboundSettingsCWProxy {
  const _$OutboundSettingsCWProxyImpl(this._value);

  final OutboundSettings _value;

  @override
  OutboundSettings allowHosts(List<String>? allowHosts) =>
      call(allowHosts: allowHosts);

  @override
  OutboundSettings allowCidrs(List<String>? allowCidrs) =>
      call(allowCidrs: allowCidrs);

  @override
  OutboundSettings denyHosts(List<String>? denyHosts) =>
      call(denyHosts: denyHosts);

  @override
  OutboundSettings denyCidrs(List<String>? denyCidrs) =>
      call(denyCidrs: denyCidrs);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OutboundSettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OutboundSettings(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OutboundSettings call({
    Object? allowHosts = const $CopyWithPlaceholder(),
    Object? allowCidrs = const $CopyWithPlaceholder(),
    Object? denyHosts = const $CopyWithPlaceholder(),
    Object? denyCidrs = const $CopyWithPlaceholder(),
  }) {
    return OutboundSettings(
      allowHosts: allowHosts == const $CopyWithPlaceholder()
          ? _value.allowHosts
          // ignore: cast_nullable_to_non_nullable
          : allowHosts as List<String>?,
      allowCidrs: allowCidrs == const $CopyWithPlaceholder()
          ? _value.allowCidrs
          // ignore: cast_nullable_to_non_nullable
          : allowCidrs as List<String>?,
      denyHosts: denyHosts == const $CopyWithPlaceholder()
          ? _value.denyHosts
          // ignore: cast_nullable_to_non_nullable
          : denyHosts as List<String>?,
      denyCidrs: denyCidrs == const $CopyWithPlaceholder()
          ? _value.denyCidrs
          // ignore: cast_nullable_to_non_nullable
          : denyCidrs as List<String>?,
    );
  }
}

extension $OutboundSettingsCopyWith on OutboundSettings {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOutboundSettings.copyWith(...)` or `instanceOfOutboundSettings.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OutboundSettingsCWProxy get copyWith => _$OutboundSettingsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OutboundSettings _$OutboundSettingsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('OutboundSettings', json, ($checkedConvert) {
      final val = OutboundSettings(
        allowHosts: $checkedConvert(
          'allowHosts',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        allowCidrs: $checkedConvert(
          'allowCidrs',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        denyHosts: $checkedConvert(
          'denyHosts',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        denyCidrs: $checkedConvert(
          'denyCidrs',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$OutboundSettingsToJson(OutboundSettings instance) =>
    <String, dynamic>{
      'allowHosts': ?instance.allowHosts,
      'allowCidrs': ?instance.allowCidrs,
      'denyHosts': ?instance.denyHosts,
      'denyCidrs': ?instance.denyCidrs,
    };
