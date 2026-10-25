// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'situation_timeline_entry.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SituationTimelineEntryCWProxy {
  SituationTimelineEntry at(DateTime at);

  SituationTimelineEntry entryType(
    SituationTimelineEntryEntryTypeEnum entryType,
  );

  SituationTimelineEntry member(SituationMember? member);

  SituationTimelineEntry action(String? action);

  SituationTimelineEntry actor(String? actor);

  SituationTimelineEntry before(Map<String, Object>? before);

  SituationTimelineEntry after(Map<String, Object>? after);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationTimelineEntry(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationTimelineEntry(...).copyWith(id: 12, name: "My name")
  /// ```
  SituationTimelineEntry call({
    DateTime at,
    SituationTimelineEntryEntryTypeEnum entryType,
    SituationMember? member,
    String? action,
    String? actor,
    Map<String, Object>? before,
    Map<String, Object>? after,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSituationTimelineEntry.copyWith(...)` or call `instanceOfSituationTimelineEntry.copyWith.fieldName(value)` for a single field.
class _$SituationTimelineEntryCWProxyImpl
    implements _$SituationTimelineEntryCWProxy {
  const _$SituationTimelineEntryCWProxyImpl(this._value);

  final SituationTimelineEntry _value;

  @override
  SituationTimelineEntry at(DateTime at) => call(at: at);

  @override
  SituationTimelineEntry entryType(
    SituationTimelineEntryEntryTypeEnum entryType,
  ) => call(entryType: entryType);

  @override
  SituationTimelineEntry member(SituationMember? member) =>
      call(member: member);

  @override
  SituationTimelineEntry action(String? action) => call(action: action);

  @override
  SituationTimelineEntry actor(String? actor) => call(actor: actor);

  @override
  SituationTimelineEntry before(Map<String, Object>? before) =>
      call(before: before);

  @override
  SituationTimelineEntry after(Map<String, Object>? after) =>
      call(after: after);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationTimelineEntry(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationTimelineEntry(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SituationTimelineEntry call({
    Object? at = const $CopyWithPlaceholder(),
    Object? entryType = const $CopyWithPlaceholder(),
    Object? member = const $CopyWithPlaceholder(),
    Object? action = const $CopyWithPlaceholder(),
    Object? actor = const $CopyWithPlaceholder(),
    Object? before = const $CopyWithPlaceholder(),
    Object? after = const $CopyWithPlaceholder(),
  }) {
    return SituationTimelineEntry(
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
      entryType: entryType == const $CopyWithPlaceholder() || entryType == null
          ? _value.entryType
          // ignore: cast_nullable_to_non_nullable
          : entryType as SituationTimelineEntryEntryTypeEnum,
      member: member == const $CopyWithPlaceholder()
          ? _value.member
          // ignore: cast_nullable_to_non_nullable
          : member as SituationMember?,
      action: action == const $CopyWithPlaceholder()
          ? _value.action
          // ignore: cast_nullable_to_non_nullable
          : action as String?,
      actor: actor == const $CopyWithPlaceholder()
          ? _value.actor
          // ignore: cast_nullable_to_non_nullable
          : actor as String?,
      before: before == const $CopyWithPlaceholder()
          ? _value.before
          // ignore: cast_nullable_to_non_nullable
          : before as Map<String, Object>?,
      after: after == const $CopyWithPlaceholder()
          ? _value.after
          // ignore: cast_nullable_to_non_nullable
          : after as Map<String, Object>?,
    );
  }
}

extension $SituationTimelineEntryCopyWith on SituationTimelineEntry {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSituationTimelineEntry.copyWith(...)` or `instanceOfSituationTimelineEntry.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SituationTimelineEntryCWProxy get copyWith =>
      _$SituationTimelineEntryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SituationTimelineEntry _$SituationTimelineEntryFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SituationTimelineEntry', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['at', 'entryType']);
  final val = SituationTimelineEntry(
    at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
    entryType: $checkedConvert(
      'entryType',
      (v) => $enumDecode(_$SituationTimelineEntryEntryTypeEnumEnumMap, v),
    ),
    member: $checkedConvert(
      'member',
      (v) => v == null
          ? null
          : SituationMember.fromJson(v as Map<String, dynamic>),
    ),
    action: $checkedConvert('action', (v) => v as String?),
    actor: $checkedConvert('actor', (v) => v as String?),
    before: $checkedConvert(
      'before',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    after: $checkedConvert(
      'after',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
  );
  return val;
});

Map<String, dynamic> _$SituationTimelineEntryToJson(
  SituationTimelineEntry instance,
) => <String, dynamic>{
  'at': instance.at.toIso8601String(),
  'entryType':
      _$SituationTimelineEntryEntryTypeEnumEnumMap[instance.entryType]!,
  'member': ?instance.member?.toJson(),
  'action': ?instance.action,
  'actor': ?instance.actor,
  'before': ?instance.before,
  'after': ?instance.after,
};

const _$SituationTimelineEntryEntryTypeEnumEnumMap = {
  SituationTimelineEntryEntryTypeEnum.member: 'member',
  SituationTimelineEntryEntryTypeEnum.log: 'log',
};
