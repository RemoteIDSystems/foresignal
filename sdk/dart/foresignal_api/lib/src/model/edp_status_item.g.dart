// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'edp_status_item.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EdpStatusItemCWProxy {
  EdpStatusItem stream(String? stream);

  EdpStatusItem type(EdpStatusItemTypeEnum type);

  EdpStatusItem health(EdpStatusItemHealthEnum health);

  EdpStatusItem message(String? message);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpStatusItem(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpStatusItem(...).copyWith(id: 12, name: "My name")
  /// ```
  EdpStatusItem call({
    String? stream,
    EdpStatusItemTypeEnum type,
    EdpStatusItemHealthEnum health,
    String? message,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEdpStatusItem.copyWith(...)` or call `instanceOfEdpStatusItem.copyWith.fieldName(value)` for a single field.
class _$EdpStatusItemCWProxyImpl implements _$EdpStatusItemCWProxy {
  const _$EdpStatusItemCWProxyImpl(this._value);

  final EdpStatusItem _value;

  @override
  EdpStatusItem stream(String? stream) => call(stream: stream);

  @override
  EdpStatusItem type(EdpStatusItemTypeEnum type) => call(type: type);

  @override
  EdpStatusItem health(EdpStatusItemHealthEnum health) => call(health: health);

  @override
  EdpStatusItem message(String? message) => call(message: message);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpStatusItem(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpStatusItem(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EdpStatusItem call({
    Object? stream = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? health = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
  }) {
    return EdpStatusItem(
      stream: stream == const $CopyWithPlaceholder()
          ? _value.stream
          // ignore: cast_nullable_to_non_nullable
          : stream as String?,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as EdpStatusItemTypeEnum,
      health: health == const $CopyWithPlaceholder() || health == null
          ? _value.health
          // ignore: cast_nullable_to_non_nullable
          : health as EdpStatusItemHealthEnum,
      message: message == const $CopyWithPlaceholder()
          ? _value.message
          // ignore: cast_nullable_to_non_nullable
          : message as String?,
    );
  }
}

extension $EdpStatusItemCopyWith on EdpStatusItem {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEdpStatusItem.copyWith(...)` or `instanceOfEdpStatusItem.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EdpStatusItemCWProxy get copyWith => _$EdpStatusItemCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EdpStatusItem _$EdpStatusItemFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EdpStatusItem', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['type', 'health']);
      final val = EdpStatusItem(
        stream: $checkedConvert('stream', (v) => v as String?),
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$EdpStatusItemTypeEnumEnumMap, v),
        ),
        health: $checkedConvert(
          'health',
          (v) => $enumDecode(_$EdpStatusItemHealthEnumEnumMap, v),
        ),
        message: $checkedConvert('message', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$EdpStatusItemToJson(EdpStatusItem instance) =>
    <String, dynamic>{
      'stream': ?instance.stream,
      'type': _$EdpStatusItemTypeEnumEnumMap[instance.type]!,
      'health': _$EdpStatusItemHealthEnumEnumMap[instance.health]!,
      'message': ?instance.message,
    };

const _$EdpStatusItemTypeEnumEnumMap = {EdpStatusItemTypeEnum.status: 'status'};

const _$EdpStatusItemHealthEnumEnumMap = {
  EdpStatusItemHealthEnum.ok: 'ok',
  EdpStatusItemHealthEnum.degraded: 'degraded',
  EdpStatusItemHealthEnum.blocked: 'blocked',
  EdpStatusItemHealthEnum.layoutChanged: 'layout-changed',
  EdpStatusItemHealthEnum.geoRestricted: 'geo-restricted',
};
