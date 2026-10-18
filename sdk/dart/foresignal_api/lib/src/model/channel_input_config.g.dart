// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'channel_input_config.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChannelInputConfigCWProxy {
  ChannelInputConfig attachMap(ChannelInputConfigAttachMapEnum? attachMap);

  ChannelInputConfig mapProducts(List<String>? mapProducts);

  ChannelInputConfig mapRadiusKm(num? mapRadiusKm);

  ChannelInputConfig maxAttachmentBytes(int? maxAttachmentBytes);

  ChannelInputConfig actions(ChannelActionsConfig? actions);

  ChannelInputConfig allowedUserIds(List<int>? allowedUserIds);

  ChannelInputConfig allowedSenders(Object? allowedSenders);

  ChannelInputConfig privatePayload(bool? privatePayload);

  ChannelInputConfig units(ChannelInputConfigUnitsEnum? units);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChannelInputConfig(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChannelInputConfig(...).copyWith(id: 12, name: "My name")
  /// ```
  ChannelInputConfig call({
    ChannelInputConfigAttachMapEnum? attachMap,
    List<String>? mapProducts,
    num? mapRadiusKm,
    int? maxAttachmentBytes,
    ChannelActionsConfig? actions,
    List<int>? allowedUserIds,
    Object? allowedSenders,
    bool? privatePayload,
    ChannelInputConfigUnitsEnum? units,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChannelInputConfig.copyWith(...)` or call `instanceOfChannelInputConfig.copyWith.fieldName(value)` for a single field.
class _$ChannelInputConfigCWProxyImpl implements _$ChannelInputConfigCWProxy {
  const _$ChannelInputConfigCWProxyImpl(this._value);

  final ChannelInputConfig _value;

  @override
  ChannelInputConfig attachMap(ChannelInputConfigAttachMapEnum? attachMap) =>
      call(attachMap: attachMap);

  @override
  ChannelInputConfig mapProducts(List<String>? mapProducts) =>
      call(mapProducts: mapProducts);

  @override
  ChannelInputConfig mapRadiusKm(num? mapRadiusKm) =>
      call(mapRadiusKm: mapRadiusKm);

  @override
  ChannelInputConfig maxAttachmentBytes(int? maxAttachmentBytes) =>
      call(maxAttachmentBytes: maxAttachmentBytes);

  @override
  ChannelInputConfig actions(ChannelActionsConfig? actions) =>
      call(actions: actions);

  @override
  ChannelInputConfig allowedUserIds(List<int>? allowedUserIds) =>
      call(allowedUserIds: allowedUserIds);

  @override
  ChannelInputConfig allowedSenders(Object? allowedSenders) =>
      call(allowedSenders: allowedSenders);

  @override
  ChannelInputConfig privatePayload(bool? privatePayload) =>
      call(privatePayload: privatePayload);

  @override
  ChannelInputConfig units(ChannelInputConfigUnitsEnum? units) =>
      call(units: units);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChannelInputConfig(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChannelInputConfig(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChannelInputConfig call({
    Object? attachMap = const $CopyWithPlaceholder(),
    Object? mapProducts = const $CopyWithPlaceholder(),
    Object? mapRadiusKm = const $CopyWithPlaceholder(),
    Object? maxAttachmentBytes = const $CopyWithPlaceholder(),
    Object? actions = const $CopyWithPlaceholder(),
    Object? allowedUserIds = const $CopyWithPlaceholder(),
    Object? allowedSenders = const $CopyWithPlaceholder(),
    Object? privatePayload = const $CopyWithPlaceholder(),
    Object? units = const $CopyWithPlaceholder(),
  }) {
    return ChannelInputConfig(
      attachMap: attachMap == const $CopyWithPlaceholder()
          ? _value.attachMap
          // ignore: cast_nullable_to_non_nullable
          : attachMap as ChannelInputConfigAttachMapEnum?,
      mapProducts: mapProducts == const $CopyWithPlaceholder()
          ? _value.mapProducts
          // ignore: cast_nullable_to_non_nullable
          : mapProducts as List<String>?,
      mapRadiusKm: mapRadiusKm == const $CopyWithPlaceholder()
          ? _value.mapRadiusKm
          // ignore: cast_nullable_to_non_nullable
          : mapRadiusKm as num?,
      maxAttachmentBytes: maxAttachmentBytes == const $CopyWithPlaceholder()
          ? _value.maxAttachmentBytes
          // ignore: cast_nullable_to_non_nullable
          : maxAttachmentBytes as int?,
      actions: actions == const $CopyWithPlaceholder()
          ? _value.actions
          // ignore: cast_nullable_to_non_nullable
          : actions as ChannelActionsConfig?,
      allowedUserIds: allowedUserIds == const $CopyWithPlaceholder()
          ? _value.allowedUserIds
          // ignore: cast_nullable_to_non_nullable
          : allowedUserIds as List<int>?,
      allowedSenders: allowedSenders == const $CopyWithPlaceholder()
          ? _value.allowedSenders
          // ignore: cast_nullable_to_non_nullable
          : allowedSenders as Object?,
      privatePayload: privatePayload == const $CopyWithPlaceholder()
          ? _value.privatePayload
          // ignore: cast_nullable_to_non_nullable
          : privatePayload as bool?,
      units: units == const $CopyWithPlaceholder()
          ? _value.units
          // ignore: cast_nullable_to_non_nullable
          : units as ChannelInputConfigUnitsEnum?,
    );
  }
}

extension $ChannelInputConfigCopyWith on ChannelInputConfig {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChannelInputConfig.copyWith(...)` or `instanceOfChannelInputConfig.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChannelInputConfigCWProxy get copyWith =>
      _$ChannelInputConfigCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChannelInputConfig _$ChannelInputConfigFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ChannelInputConfig', json, ($checkedConvert) {
      final val = ChannelInputConfig(
        attachMap: $checkedConvert(
          'attachMap',
          (v) =>
              $enumDecodeNullable(_$ChannelInputConfigAttachMapEnumEnumMap, v),
        ),
        mapProducts: $checkedConvert(
          'mapProducts',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        mapRadiusKm: $checkedConvert('mapRadiusKm', (v) => v as num? ?? 50),
        maxAttachmentBytes: $checkedConvert(
          'maxAttachmentBytes',
          (v) => (v as num?)?.toInt(),
        ),
        actions: $checkedConvert(
          'actions',
          (v) => v == null
              ? null
              : ChannelActionsConfig.fromJson(v as Map<String, dynamic>),
        ),
        allowedUserIds: $checkedConvert(
          'allowedUserIds',
          (v) => (v as List<dynamic>?)?.map((e) => (e as num).toInt()).toList(),
        ),
        allowedSenders: $checkedConvert('allowedSenders', (v) => v),
        privatePayload: $checkedConvert(
          'privatePayload',
          (v) => v as bool? ?? false,
        ),
        units: $checkedConvert(
          'units',
          (v) => $enumDecodeNullable(_$ChannelInputConfigUnitsEnumEnumMap, v),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ChannelInputConfigToJson(
  ChannelInputConfig instance,
) => <String, dynamic>{
  'attachMap': ?_$ChannelInputConfigAttachMapEnumEnumMap[instance.attachMap],
  'mapProducts': ?instance.mapProducts,
  'mapRadiusKm': ?instance.mapRadiusKm,
  'maxAttachmentBytes': ?instance.maxAttachmentBytes,
  'actions': ?instance.actions?.toJson(),
  'allowedUserIds': ?instance.allowedUserIds,
  'allowedSenders': ?instance.allowedSenders,
  'privatePayload': ?instance.privatePayload,
  'units': ?_$ChannelInputConfigUnitsEnumEnumMap[instance.units],
};

const _$ChannelInputConfigAttachMapEnumEnumMap = {
  ChannelInputConfigAttachMapEnum.none: 'none',
  ChannelInputConfigAttachMapEnum.static_: 'static',
};

const _$ChannelInputConfigUnitsEnumEnumMap = {
  ChannelInputConfigUnitsEnum.metric: 'metric',
  ChannelInputConfigUnitsEnum.imperial: 'imperial',
};
