// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'delivery_target.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DeliveryTargetCWProxy {
  DeliveryTarget kind(DeliveryTargetKindEnum kind);

  DeliveryTarget channelId(String? channelId);

  DeliveryTarget format(DeliveryTargetFormatEnum format);

  DeliveryTarget options(Map<String, Object>? options);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DeliveryTarget(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DeliveryTarget(...).copyWith(id: 12, name: "My name")
  /// ```
  DeliveryTarget call({
    DeliveryTargetKindEnum kind,
    String? channelId,
    DeliveryTargetFormatEnum format,
    Map<String, Object>? options,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDeliveryTarget.copyWith(...)` or call `instanceOfDeliveryTarget.copyWith.fieldName(value)` for a single field.
class _$DeliveryTargetCWProxyImpl implements _$DeliveryTargetCWProxy {
  const _$DeliveryTargetCWProxyImpl(this._value);

  final DeliveryTarget _value;

  @override
  DeliveryTarget kind(DeliveryTargetKindEnum kind) => call(kind: kind);

  @override
  DeliveryTarget channelId(String? channelId) => call(channelId: channelId);

  @override
  DeliveryTarget format(DeliveryTargetFormatEnum format) =>
      call(format: format);

  @override
  DeliveryTarget options(Map<String, Object>? options) =>
      call(options: options);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DeliveryTarget(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DeliveryTarget(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DeliveryTarget call({
    Object? kind = const $CopyWithPlaceholder(),
    Object? channelId = const $CopyWithPlaceholder(),
    Object? format = const $CopyWithPlaceholder(),
    Object? options = const $CopyWithPlaceholder(),
  }) {
    return DeliveryTarget(
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as DeliveryTargetKindEnum,
      channelId: channelId == const $CopyWithPlaceholder()
          ? _value.channelId
          // ignore: cast_nullable_to_non_nullable
          : channelId as String?,
      format: format == const $CopyWithPlaceholder() || format == null
          ? _value.format
          // ignore: cast_nullable_to_non_nullable
          : format as DeliveryTargetFormatEnum,
      options: options == const $CopyWithPlaceholder()
          ? _value.options
          // ignore: cast_nullable_to_non_nullable
          : options as Map<String, Object>?,
    );
  }
}

extension $DeliveryTargetCopyWith on DeliveryTarget {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDeliveryTarget.copyWith(...)` or `instanceOfDeliveryTarget.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DeliveryTargetCWProxy get copyWith => _$DeliveryTargetCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DeliveryTarget _$DeliveryTargetFromJson(Map<String, dynamic> json) =>
    $checkedCreate('DeliveryTarget', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['kind', 'format']);
      final val = DeliveryTarget(
        kind: $checkedConvert(
          'kind',
          (v) => $enumDecode(_$DeliveryTargetKindEnumEnumMap, v),
        ),
        channelId: $checkedConvert('channelId', (v) => v as String?),
        format: $checkedConvert(
          'format',
          (v) => $enumDecode(_$DeliveryTargetFormatEnumEnumMap, v),
        ),
        options: $checkedConvert(
          'options',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as Object),
          ),
        ),
      );
      return val;
    });

Map<String, dynamic> _$DeliveryTargetToJson(DeliveryTarget instance) =>
    <String, dynamic>{
      'kind': _$DeliveryTargetKindEnumEnumMap[instance.kind]!,
      'channelId': ?instance.channelId,
      'format': _$DeliveryTargetFormatEnumEnumMap[instance.format]!,
      'options': ?instance.options,
    };

const _$DeliveryTargetKindEnumEnumMap = {
  DeliveryTargetKindEnum.channel: 'channel',
  DeliveryTargetKindEnum.website: 'website',
  DeliveryTargetKindEnum.podcast: 'podcast',
};

const _$DeliveryTargetFormatEnumEnumMap = {
  DeliveryTargetFormatEnum.email: 'email',
  DeliveryTargetFormatEnum.push: 'push',
  DeliveryTargetFormatEnum.alert: 'alert',
  DeliveryTargetFormatEnum.html: 'html',
  DeliveryTargetFormatEnum.audio: 'audio',
};
