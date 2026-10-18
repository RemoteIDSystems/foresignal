// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'precursor_event_precursor.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PrecursorEventPrecursorCWProxy {
  PrecursorEventPrecursor id(String id);

  PrecursorEventPrecursor kind(PrecursorEventPrecursorKindEnum kind);

  PrecursorEventPrecursor status(PrecursorEventPrecursorStatusEnum status);

  PrecursorEventPrecursor statusReason(String? statusReason);

  PrecursorEventPrecursor description(String description);

  PrecursorEventPrecursor lift(num? lift);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorEventPrecursor(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorEventPrecursor(...).copyWith(id: 12, name: "My name")
  /// ```
  PrecursorEventPrecursor call({
    String id,
    PrecursorEventPrecursorKindEnum kind,
    PrecursorEventPrecursorStatusEnum status,
    String? statusReason,
    String description,
    num? lift,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPrecursorEventPrecursor.copyWith(...)` or call `instanceOfPrecursorEventPrecursor.copyWith.fieldName(value)` for a single field.
class _$PrecursorEventPrecursorCWProxyImpl
    implements _$PrecursorEventPrecursorCWProxy {
  const _$PrecursorEventPrecursorCWProxyImpl(this._value);

  final PrecursorEventPrecursor _value;

  @override
  PrecursorEventPrecursor id(String id) => call(id: id);

  @override
  PrecursorEventPrecursor kind(PrecursorEventPrecursorKindEnum kind) =>
      call(kind: kind);

  @override
  PrecursorEventPrecursor status(PrecursorEventPrecursorStatusEnum status) =>
      call(status: status);

  @override
  PrecursorEventPrecursor statusReason(String? statusReason) =>
      call(statusReason: statusReason);

  @override
  PrecursorEventPrecursor description(String description) =>
      call(description: description);

  @override
  PrecursorEventPrecursor lift(num? lift) => call(lift: lift);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorEventPrecursor(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorEventPrecursor(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PrecursorEventPrecursor call({
    Object? id = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? statusReason = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? lift = const $CopyWithPlaceholder(),
  }) {
    return PrecursorEventPrecursor(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as PrecursorEventPrecursorKindEnum,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as PrecursorEventPrecursorStatusEnum,
      statusReason: statusReason == const $CopyWithPlaceholder()
          ? _value.statusReason
          // ignore: cast_nullable_to_non_nullable
          : statusReason as String?,
      description:
          description == const $CopyWithPlaceholder() || description == null
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String,
      lift: lift == const $CopyWithPlaceholder()
          ? _value.lift
          // ignore: cast_nullable_to_non_nullable
          : lift as num?,
    );
  }
}

extension $PrecursorEventPrecursorCopyWith on PrecursorEventPrecursor {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPrecursorEventPrecursor.copyWith(...)` or `instanceOfPrecursorEventPrecursor.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PrecursorEventPrecursorCWProxy get copyWith =>
      _$PrecursorEventPrecursorCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PrecursorEventPrecursor _$PrecursorEventPrecursorFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PrecursorEventPrecursor', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['id', 'kind', 'status', 'description']);
  final val = PrecursorEventPrecursor(
    id: $checkedConvert('id', (v) => v as String),
    kind: $checkedConvert(
      'kind',
      (v) => $enumDecode(_$PrecursorEventPrecursorKindEnumEnumMap, v),
    ),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$PrecursorEventPrecursorStatusEnumEnumMap, v),
    ),
    statusReason: $checkedConvert('statusReason', (v) => v as String?),
    description: $checkedConvert('description', (v) => v as String),
    lift: $checkedConvert('lift', (v) => v as num?),
  );
  return val;
});

Map<String, dynamic> _$PrecursorEventPrecursorToJson(
  PrecursorEventPrecursor instance,
) => <String, dynamic>{
  'id': instance.id,
  'kind': _$PrecursorEventPrecursorKindEnumEnumMap[instance.kind]!,
  'status': _$PrecursorEventPrecursorStatusEnumEnumMap[instance.status]!,
  'statusReason': ?instance.statusReason,
  'description': instance.description,
  'lift': ?instance.lift,
};

const _$PrecursorEventPrecursorKindEnumEnumMap = {
  PrecursorEventPrecursorKindEnum.event: 'event',
  PrecursorEventPrecursorKindEnum.series: 'series',
};

const _$PrecursorEventPrecursorStatusEnumEnumMap = {
  PrecursorEventPrecursorStatusEnum.candidate: 'candidate',
  PrecursorEventPrecursorStatusEnum.validated: 'validated',
  PrecursorEventPrecursorStatusEnum.stale: 'stale',
  PrecursorEventPrecursorStatusEnum.retired: 'retired',
};
