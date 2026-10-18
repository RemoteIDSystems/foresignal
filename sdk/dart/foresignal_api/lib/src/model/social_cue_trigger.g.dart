// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_cue_trigger.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialCueTriggerCWProxy {
  SocialCueTrigger type(SocialCueTriggerTypeEnum type);

  SocialCueTrigger ref(String? ref);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialCueTrigger(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialCueTrigger(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialCueTrigger call({SocialCueTriggerTypeEnum type, String? ref});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialCueTrigger.copyWith(...)` or call `instanceOfSocialCueTrigger.copyWith.fieldName(value)` for a single field.
class _$SocialCueTriggerCWProxyImpl implements _$SocialCueTriggerCWProxy {
  const _$SocialCueTriggerCWProxyImpl(this._value);

  final SocialCueTrigger _value;

  @override
  SocialCueTrigger type(SocialCueTriggerTypeEnum type) => call(type: type);

  @override
  SocialCueTrigger ref(String? ref) => call(ref: ref);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialCueTrigger(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialCueTrigger(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialCueTrigger call({
    Object? type = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
  }) {
    return SocialCueTrigger(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as SocialCueTriggerTypeEnum,
      ref: ref == const $CopyWithPlaceholder()
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String?,
    );
  }
}

extension $SocialCueTriggerCopyWith on SocialCueTrigger {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialCueTrigger.copyWith(...)` or `instanceOfSocialCueTrigger.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialCueTriggerCWProxy get copyWith => _$SocialCueTriggerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialCueTrigger _$SocialCueTriggerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SocialCueTrigger', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['type']);
      final val = SocialCueTrigger(
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$SocialCueTriggerTypeEnumEnumMap, v),
        ),
        ref: $checkedConvert('ref', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$SocialCueTriggerToJson(SocialCueTrigger instance) =>
    <String, dynamic>{
      'type': _$SocialCueTriggerTypeEnumEnumMap[instance.type]!,
      'ref': ?instance.ref,
    };

const _$SocialCueTriggerTypeEnumEnumMap = {
  SocialCueTriggerTypeEnum.situationActivated: 'situation-activated',
  SocialCueTriggerTypeEnum.situationEscalated: 'situation-escalated',
  SocialCueTriggerTypeEnum.finding: 'finding',
  SocialCueTriggerTypeEnum.anomaly: 'anomaly',
  SocialCueTriggerTypeEnum.alert: 'alert',
  SocialCueTriggerTypeEnum.canonicalEvent: 'canonical-event',
  SocialCueTriggerTypeEnum.manual: 'manual',
};
