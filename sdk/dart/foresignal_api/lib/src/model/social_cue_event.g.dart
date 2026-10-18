// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_cue_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialCueEventCWProxy {
  SocialCueEvent event(SocialCueEventEventEnum event);

  SocialCueEvent cue(SocialCue cue);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialCueEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialCueEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialCueEvent call({SocialCueEventEventEnum event, SocialCue cue});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialCueEvent.copyWith(...)` or call `instanceOfSocialCueEvent.copyWith.fieldName(value)` for a single field.
class _$SocialCueEventCWProxyImpl implements _$SocialCueEventCWProxy {
  const _$SocialCueEventCWProxyImpl(this._value);

  final SocialCueEvent _value;

  @override
  SocialCueEvent event(SocialCueEventEventEnum event) => call(event: event);

  @override
  SocialCueEvent cue(SocialCue cue) => call(cue: cue);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialCueEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialCueEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialCueEvent call({
    Object? event = const $CopyWithPlaceholder(),
    Object? cue = const $CopyWithPlaceholder(),
  }) {
    return SocialCueEvent(
      event: event == const $CopyWithPlaceholder() || event == null
          ? _value.event
          // ignore: cast_nullable_to_non_nullable
          : event as SocialCueEventEventEnum,
      cue: cue == const $CopyWithPlaceholder() || cue == null
          ? _value.cue
          // ignore: cast_nullable_to_non_nullable
          : cue as SocialCue,
    );
  }
}

extension $SocialCueEventCopyWith on SocialCueEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialCueEvent.copyWith(...)` or `instanceOfSocialCueEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialCueEventCWProxy get copyWith => _$SocialCueEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialCueEvent _$SocialCueEventFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SocialCueEvent', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['event', 'cue']);
      final val = SocialCueEvent(
        event: $checkedConvert(
          'event',
          (v) => $enumDecode(_$SocialCueEventEventEnumEnumMap, v),
        ),
        cue: $checkedConvert(
          'cue',
          (v) => SocialCue.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$SocialCueEventToJson(SocialCueEvent instance) =>
    <String, dynamic>{
      'event': _$SocialCueEventEventEnumEnumMap[instance.event]!,
      'cue': instance.cue.toJson(),
    };

const _$SocialCueEventEventEnumEnumMap = {
  SocialCueEventEventEnum.cuePeriodCompleted: 'cue.completed',
};
