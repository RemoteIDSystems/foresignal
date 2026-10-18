// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'situation_changes.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SituationChangesCWProxy {
  SituationChanges situationId(String situationId);

  SituationChanges since(DateTime since);

  SituationChanges baselineDay(DateTime? baselineDay);

  SituationChanges now(DateTime now);

  SituationChanges status(SituationChangesStatus status);

  SituationChanges severity(SituationChangesSeverity severity);

  SituationChanges newMembers(SituationChangesNewMembers newMembers);

  SituationChanges area(SituationChangesArea? area);

  SituationChanges regionsAdded(List<String> regionsAdded);

  SituationChanges regionsRemoved(List<String>? regionsRemoved);

  SituationChanges keyValues(List<SituationChangesKeyValuesInner>? keyValues);

  SituationChanges manifestId(String? manifestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationChanges(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationChanges(...).copyWith(id: 12, name: "My name")
  /// ```
  SituationChanges call({
    String situationId,
    DateTime since,
    DateTime? baselineDay,
    DateTime now,
    SituationChangesStatus status,
    SituationChangesSeverity severity,
    SituationChangesNewMembers newMembers,
    SituationChangesArea? area,
    List<String> regionsAdded,
    List<String>? regionsRemoved,
    List<SituationChangesKeyValuesInner>? keyValues,
    String? manifestId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSituationChanges.copyWith(...)` or call `instanceOfSituationChanges.copyWith.fieldName(value)` for a single field.
class _$SituationChangesCWProxyImpl implements _$SituationChangesCWProxy {
  const _$SituationChangesCWProxyImpl(this._value);

  final SituationChanges _value;

  @override
  SituationChanges situationId(String situationId) =>
      call(situationId: situationId);

  @override
  SituationChanges since(DateTime since) => call(since: since);

  @override
  SituationChanges baselineDay(DateTime? baselineDay) =>
      call(baselineDay: baselineDay);

  @override
  SituationChanges now(DateTime now) => call(now: now);

  @override
  SituationChanges status(SituationChangesStatus status) =>
      call(status: status);

  @override
  SituationChanges severity(SituationChangesSeverity severity) =>
      call(severity: severity);

  @override
  SituationChanges newMembers(SituationChangesNewMembers newMembers) =>
      call(newMembers: newMembers);

  @override
  SituationChanges area(SituationChangesArea? area) => call(area: area);

  @override
  SituationChanges regionsAdded(List<String> regionsAdded) =>
      call(regionsAdded: regionsAdded);

  @override
  SituationChanges regionsRemoved(List<String>? regionsRemoved) =>
      call(regionsRemoved: regionsRemoved);

  @override
  SituationChanges keyValues(List<SituationChangesKeyValuesInner>? keyValues) =>
      call(keyValues: keyValues);

  @override
  SituationChanges manifestId(String? manifestId) =>
      call(manifestId: manifestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationChanges(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationChanges(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SituationChanges call({
    Object? situationId = const $CopyWithPlaceholder(),
    Object? since = const $CopyWithPlaceholder(),
    Object? baselineDay = const $CopyWithPlaceholder(),
    Object? now = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? severity = const $CopyWithPlaceholder(),
    Object? newMembers = const $CopyWithPlaceholder(),
    Object? area = const $CopyWithPlaceholder(),
    Object? regionsAdded = const $CopyWithPlaceholder(),
    Object? regionsRemoved = const $CopyWithPlaceholder(),
    Object? keyValues = const $CopyWithPlaceholder(),
    Object? manifestId = const $CopyWithPlaceholder(),
  }) {
    return SituationChanges(
      situationId:
          situationId == const $CopyWithPlaceholder() || situationId == null
          ? _value.situationId
          // ignore: cast_nullable_to_non_nullable
          : situationId as String,
      since: since == const $CopyWithPlaceholder() || since == null
          ? _value.since
          // ignore: cast_nullable_to_non_nullable
          : since as DateTime,
      baselineDay: baselineDay == const $CopyWithPlaceholder()
          ? _value.baselineDay
          // ignore: cast_nullable_to_non_nullable
          : baselineDay as DateTime?,
      now: now == const $CopyWithPlaceholder() || now == null
          ? _value.now
          // ignore: cast_nullable_to_non_nullable
          : now as DateTime,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as SituationChangesStatus,
      severity: severity == const $CopyWithPlaceholder() || severity == null
          ? _value.severity
          // ignore: cast_nullable_to_non_nullable
          : severity as SituationChangesSeverity,
      newMembers:
          newMembers == const $CopyWithPlaceholder() || newMembers == null
          ? _value.newMembers
          // ignore: cast_nullable_to_non_nullable
          : newMembers as SituationChangesNewMembers,
      area: area == const $CopyWithPlaceholder()
          ? _value.area
          // ignore: cast_nullable_to_non_nullable
          : area as SituationChangesArea?,
      regionsAdded:
          regionsAdded == const $CopyWithPlaceholder() || regionsAdded == null
          ? _value.regionsAdded
          // ignore: cast_nullable_to_non_nullable
          : regionsAdded as List<String>,
      regionsRemoved: regionsRemoved == const $CopyWithPlaceholder()
          ? _value.regionsRemoved
          // ignore: cast_nullable_to_non_nullable
          : regionsRemoved as List<String>?,
      keyValues: keyValues == const $CopyWithPlaceholder()
          ? _value.keyValues
          // ignore: cast_nullable_to_non_nullable
          : keyValues as List<SituationChangesKeyValuesInner>?,
      manifestId: manifestId == const $CopyWithPlaceholder()
          ? _value.manifestId
          // ignore: cast_nullable_to_non_nullable
          : manifestId as String?,
    );
  }
}

extension $SituationChangesCopyWith on SituationChanges {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSituationChanges.copyWith(...)` or `instanceOfSituationChanges.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SituationChangesCWProxy get copyWith => _$SituationChangesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SituationChanges _$SituationChangesFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SituationChanges', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'situationId',
          'since',
          'now',
          'status',
          'severity',
          'newMembers',
          'regionsAdded',
        ],
      );
      final val = SituationChanges(
        situationId: $checkedConvert('situationId', (v) => v as String),
        since: $checkedConvert('since', (v) => DateTime.parse(v as String)),
        baselineDay: $checkedConvert(
          'baselineDay',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        now: $checkedConvert('now', (v) => DateTime.parse(v as String)),
        status: $checkedConvert(
          'status',
          (v) => SituationChangesStatus.fromJson(v as Map<String, dynamic>),
        ),
        severity: $checkedConvert(
          'severity',
          (v) => SituationChangesSeverity.fromJson(v as Map<String, dynamic>),
        ),
        newMembers: $checkedConvert(
          'newMembers',
          (v) => SituationChangesNewMembers.fromJson(v as Map<String, dynamic>),
        ),
        area: $checkedConvert(
          'area',
          (v) => v == null
              ? null
              : SituationChangesArea.fromJson(v as Map<String, dynamic>),
        ),
        regionsAdded: $checkedConvert(
          'regionsAdded',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
        regionsRemoved: $checkedConvert(
          'regionsRemoved',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        keyValues: $checkedConvert(
          'keyValues',
          (v) => (v as List<dynamic>?)
              ?.map(
                (e) => SituationChangesKeyValuesInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
        manifestId: $checkedConvert('manifestId', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$SituationChangesToJson(SituationChanges instance) =>
    <String, dynamic>{
      'situationId': instance.situationId,
      'since': instance.since.toIso8601String(),
      'baselineDay': ?instance.baselineDay?.toIso8601String(),
      'now': instance.now.toIso8601String(),
      'status': instance.status.toJson(),
      'severity': instance.severity.toJson(),
      'newMembers': instance.newMembers.toJson(),
      'area': ?instance.area?.toJson(),
      'regionsAdded': instance.regionsAdded,
      'regionsRemoved': ?instance.regionsRemoved,
      'keyValues': ?instance.keyValues?.map((e) => e.toJson()).toList(),
      'manifestId': ?instance.manifestId,
    };
