// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'presence_event_change.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PresenceEventChangeCWProxy {
  PresenceEventChange type(PresenceEventChangeTypeEnum type);

  PresenceEventChange clientId(String clientId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PresenceEventChange(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PresenceEventChange(...).copyWith(id: 12, name: "My name")
  /// ```
  PresenceEventChange call({PresenceEventChangeTypeEnum type, String clientId});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPresenceEventChange.copyWith(...)` or call `instanceOfPresenceEventChange.copyWith.fieldName(value)` for a single field.
class _$PresenceEventChangeCWProxyImpl implements _$PresenceEventChangeCWProxy {
  const _$PresenceEventChangeCWProxyImpl(this._value);

  final PresenceEventChange _value;

  @override
  PresenceEventChange type(PresenceEventChangeTypeEnum type) =>
      call(type: type);

  @override
  PresenceEventChange clientId(String clientId) => call(clientId: clientId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PresenceEventChange(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PresenceEventChange(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PresenceEventChange call({
    Object? type = const $CopyWithPlaceholder(),
    Object? clientId = const $CopyWithPlaceholder(),
  }) {
    return PresenceEventChange(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as PresenceEventChangeTypeEnum,
      clientId: clientId == const $CopyWithPlaceholder() || clientId == null
          ? _value.clientId
          // ignore: cast_nullable_to_non_nullable
          : clientId as String,
    );
  }
}

extension $PresenceEventChangeCopyWith on PresenceEventChange {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPresenceEventChange.copyWith(...)` or `instanceOfPresenceEventChange.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PresenceEventChangeCWProxy get copyWith =>
      _$PresenceEventChangeCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PresenceEventChange _$PresenceEventChangeFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PresenceEventChange', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['type', 'clientId']);
      final val = PresenceEventChange(
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$PresenceEventChangeTypeEnumEnumMap, v),
        ),
        clientId: $checkedConvert('clientId', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$PresenceEventChangeToJson(
  PresenceEventChange instance,
) => <String, dynamic>{
  'type': _$PresenceEventChangeTypeEnumEnumMap[instance.type]!,
  'clientId': instance.clientId,
};

const _$PresenceEventChangeTypeEnumEnumMap = {
  PresenceEventChangeTypeEnum.joined: 'joined',
  PresenceEventChangeTypeEnum.left: 'left',
};
