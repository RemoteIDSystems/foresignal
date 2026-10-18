// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'situation_changes_new_members.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SituationChangesNewMembersCWProxy {
  SituationChangesNewMembers total(int total);

  SituationChangesNewMembers byFamily(Map<String, int> byFamily);

  SituationChangesNewMembers byType(Map<String, int>? byType);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationChangesNewMembers(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationChangesNewMembers(...).copyWith(id: 12, name: "My name")
  /// ```
  SituationChangesNewMembers call({
    int total,
    Map<String, int> byFamily,
    Map<String, int>? byType,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSituationChangesNewMembers.copyWith(...)` or call `instanceOfSituationChangesNewMembers.copyWith.fieldName(value)` for a single field.
class _$SituationChangesNewMembersCWProxyImpl
    implements _$SituationChangesNewMembersCWProxy {
  const _$SituationChangesNewMembersCWProxyImpl(this._value);

  final SituationChangesNewMembers _value;

  @override
  SituationChangesNewMembers total(int total) => call(total: total);

  @override
  SituationChangesNewMembers byFamily(Map<String, int> byFamily) =>
      call(byFamily: byFamily);

  @override
  SituationChangesNewMembers byType(Map<String, int>? byType) =>
      call(byType: byType);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationChangesNewMembers(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationChangesNewMembers(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SituationChangesNewMembers call({
    Object? total = const $CopyWithPlaceholder(),
    Object? byFamily = const $CopyWithPlaceholder(),
    Object? byType = const $CopyWithPlaceholder(),
  }) {
    return SituationChangesNewMembers(
      total: total == const $CopyWithPlaceholder() || total == null
          ? _value.total
          // ignore: cast_nullable_to_non_nullable
          : total as int,
      byFamily: byFamily == const $CopyWithPlaceholder() || byFamily == null
          ? _value.byFamily
          // ignore: cast_nullable_to_non_nullable
          : byFamily as Map<String, int>,
      byType: byType == const $CopyWithPlaceholder()
          ? _value.byType
          // ignore: cast_nullable_to_non_nullable
          : byType as Map<String, int>?,
    );
  }
}

extension $SituationChangesNewMembersCopyWith on SituationChangesNewMembers {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSituationChangesNewMembers.copyWith(...)` or `instanceOfSituationChangesNewMembers.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SituationChangesNewMembersCWProxy get copyWith =>
      _$SituationChangesNewMembersCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SituationChangesNewMembers _$SituationChangesNewMembersFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SituationChangesNewMembers', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['total', 'byFamily']);
  final val = SituationChangesNewMembers(
    total: $checkedConvert('total', (v) => (v as num).toInt()),
    byFamily: $checkedConvert(
      'byFamily',
      (v) => Map<String, int>.from(v as Map),
    ),
    byType: $checkedConvert(
      'byType',
      (v) => (v as Map<String, dynamic>?)?.map(
        (k, e) => MapEntry(k, (e as num).toInt()),
      ),
    ),
  );
  return val;
});

Map<String, dynamic> _$SituationChangesNewMembersToJson(
  SituationChangesNewMembers instance,
) => <String, dynamic>{
  'total': instance.total,
  'byFamily': instance.byFamily,
  'byType': ?instance.byType,
};
