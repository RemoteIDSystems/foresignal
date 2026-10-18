// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'situation_changes_status.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SituationChangesStatusCWProxy {
  SituationChangesStatus before(SituationStatus? before);

  SituationChangesStatus after(SituationStatus after);

  SituationChangesStatus changed(bool changed);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationChangesStatus(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationChangesStatus(...).copyWith(id: 12, name: "My name")
  /// ```
  SituationChangesStatus call({
    SituationStatus? before,
    SituationStatus after,
    bool changed,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSituationChangesStatus.copyWith(...)` or call `instanceOfSituationChangesStatus.copyWith.fieldName(value)` for a single field.
class _$SituationChangesStatusCWProxyImpl
    implements _$SituationChangesStatusCWProxy {
  const _$SituationChangesStatusCWProxyImpl(this._value);

  final SituationChangesStatus _value;

  @override
  SituationChangesStatus before(SituationStatus? before) =>
      call(before: before);

  @override
  SituationChangesStatus after(SituationStatus after) => call(after: after);

  @override
  SituationChangesStatus changed(bool changed) => call(changed: changed);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationChangesStatus(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationChangesStatus(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SituationChangesStatus call({
    Object? before = const $CopyWithPlaceholder(),
    Object? after = const $CopyWithPlaceholder(),
    Object? changed = const $CopyWithPlaceholder(),
  }) {
    return SituationChangesStatus(
      before: before == const $CopyWithPlaceholder()
          ? _value.before
          // ignore: cast_nullable_to_non_nullable
          : before as SituationStatus?,
      after: after == const $CopyWithPlaceholder() || after == null
          ? _value.after
          // ignore: cast_nullable_to_non_nullable
          : after as SituationStatus,
      changed: changed == const $CopyWithPlaceholder() || changed == null
          ? _value.changed
          // ignore: cast_nullable_to_non_nullable
          : changed as bool,
    );
  }
}

extension $SituationChangesStatusCopyWith on SituationChangesStatus {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSituationChangesStatus.copyWith(...)` or `instanceOfSituationChangesStatus.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SituationChangesStatusCWProxy get copyWith =>
      _$SituationChangesStatusCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SituationChangesStatus _$SituationChangesStatusFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SituationChangesStatus', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['after', 'changed']);
  final val = SituationChangesStatus(
    before: $checkedConvert(
      'before',
      (v) => $enumDecodeNullable(_$SituationStatusEnumMap, v),
    ),
    after: $checkedConvert(
      'after',
      (v) => $enumDecode(_$SituationStatusEnumMap, v),
    ),
    changed: $checkedConvert('changed', (v) => v as bool),
  );
  return val;
});

Map<String, dynamic> _$SituationChangesStatusToJson(
  SituationChangesStatus instance,
) => <String, dynamic>{
  'before': ?_$SituationStatusEnumMap[instance.before],
  'after': _$SituationStatusEnumMap[instance.after]!,
  'changed': instance.changed,
};

const _$SituationStatusEnumMap = {
  SituationStatus.emerging: 'emerging',
  SituationStatus.active: 'active',
  SituationStatus.dormant: 'dormant',
  SituationStatus.closed: 'closed',
};
