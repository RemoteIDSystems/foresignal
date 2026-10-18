// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'situation_brief_status.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SituationBriefStatusCWProxy {
  SituationBriefStatus situationId(String situationId);

  SituationBriefStatus status(SituationBriefStatusStatusEnum status);

  SituationBriefStatus requestedAt(DateTime requestedAt);

  SituationBriefStatus previousBriefGeneratedAt(
    DateTime? previousBriefGeneratedAt,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationBriefStatus(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationBriefStatus(...).copyWith(id: 12, name: "My name")
  /// ```
  SituationBriefStatus call({
    String situationId,
    SituationBriefStatusStatusEnum status,
    DateTime requestedAt,
    DateTime? previousBriefGeneratedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSituationBriefStatus.copyWith(...)` or call `instanceOfSituationBriefStatus.copyWith.fieldName(value)` for a single field.
class _$SituationBriefStatusCWProxyImpl
    implements _$SituationBriefStatusCWProxy {
  const _$SituationBriefStatusCWProxyImpl(this._value);

  final SituationBriefStatus _value;

  @override
  SituationBriefStatus situationId(String situationId) =>
      call(situationId: situationId);

  @override
  SituationBriefStatus status(SituationBriefStatusStatusEnum status) =>
      call(status: status);

  @override
  SituationBriefStatus requestedAt(DateTime requestedAt) =>
      call(requestedAt: requestedAt);

  @override
  SituationBriefStatus previousBriefGeneratedAt(
    DateTime? previousBriefGeneratedAt,
  ) => call(previousBriefGeneratedAt: previousBriefGeneratedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationBriefStatus(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationBriefStatus(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SituationBriefStatus call({
    Object? situationId = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? requestedAt = const $CopyWithPlaceholder(),
    Object? previousBriefGeneratedAt = const $CopyWithPlaceholder(),
  }) {
    return SituationBriefStatus(
      situationId:
          situationId == const $CopyWithPlaceholder() || situationId == null
          ? _value.situationId
          // ignore: cast_nullable_to_non_nullable
          : situationId as String,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as SituationBriefStatusStatusEnum,
      requestedAt:
          requestedAt == const $CopyWithPlaceholder() || requestedAt == null
          ? _value.requestedAt
          // ignore: cast_nullable_to_non_nullable
          : requestedAt as DateTime,
      previousBriefGeneratedAt:
          previousBriefGeneratedAt == const $CopyWithPlaceholder()
          ? _value.previousBriefGeneratedAt
          // ignore: cast_nullable_to_non_nullable
          : previousBriefGeneratedAt as DateTime?,
    );
  }
}

extension $SituationBriefStatusCopyWith on SituationBriefStatus {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSituationBriefStatus.copyWith(...)` or `instanceOfSituationBriefStatus.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SituationBriefStatusCWProxy get copyWith =>
      _$SituationBriefStatusCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SituationBriefStatus _$SituationBriefStatusFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SituationBriefStatus', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['situationId', 'status', 'requestedAt'],
  );
  final val = SituationBriefStatus(
    situationId: $checkedConvert('situationId', (v) => v as String),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$SituationBriefStatusStatusEnumEnumMap, v),
    ),
    requestedAt: $checkedConvert(
      'requestedAt',
      (v) => DateTime.parse(v as String),
    ),
    previousBriefGeneratedAt: $checkedConvert(
      'previousBriefGeneratedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$SituationBriefStatusToJson(
  SituationBriefStatus instance,
) => <String, dynamic>{
  'situationId': instance.situationId,
  'status': _$SituationBriefStatusStatusEnumEnumMap[instance.status]!,
  'requestedAt': instance.requestedAt.toIso8601String(),
  'previousBriefGeneratedAt': ?instance.previousBriefGeneratedAt
      ?.toIso8601String(),
};

const _$SituationBriefStatusStatusEnumEnumMap = {
  SituationBriefStatusStatusEnum.queued: 'queued',
  SituationBriefStatusStatusEnum.running: 'running',
};
