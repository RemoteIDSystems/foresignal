// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'intel_event_patch.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IntelEventPatchCWProxy {
  IntelEventPatch title(String? title);

  IntelEventPatch status(IntelEventPatchStatusEnum? status);

  IntelEventPatch severity(Severity? severity);

  IntelEventPatch notes(String? notes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelEventPatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelEventPatch(...).copyWith(id: 12, name: "My name")
  /// ```
  IntelEventPatch call({
    String? title,
    IntelEventPatchStatusEnum? status,
    Severity? severity,
    String? notes,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIntelEventPatch.copyWith(...)` or call `instanceOfIntelEventPatch.copyWith.fieldName(value)` for a single field.
class _$IntelEventPatchCWProxyImpl implements _$IntelEventPatchCWProxy {
  const _$IntelEventPatchCWProxyImpl(this._value);

  final IntelEventPatch _value;

  @override
  IntelEventPatch title(String? title) => call(title: title);

  @override
  IntelEventPatch status(IntelEventPatchStatusEnum? status) =>
      call(status: status);

  @override
  IntelEventPatch severity(Severity? severity) => call(severity: severity);

  @override
  IntelEventPatch notes(String? notes) => call(notes: notes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelEventPatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelEventPatch(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IntelEventPatch call({
    Object? title = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? severity = const $CopyWithPlaceholder(),
    Object? notes = const $CopyWithPlaceholder(),
  }) {
    return IntelEventPatch(
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      status: status == const $CopyWithPlaceholder()
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as IntelEventPatchStatusEnum?,
      severity: severity == const $CopyWithPlaceholder()
          ? _value.severity
          // ignore: cast_nullable_to_non_nullable
          : severity as Severity?,
      notes: notes == const $CopyWithPlaceholder()
          ? _value.notes
          // ignore: cast_nullable_to_non_nullable
          : notes as String?,
    );
  }
}

extension $IntelEventPatchCopyWith on IntelEventPatch {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIntelEventPatch.copyWith(...)` or `instanceOfIntelEventPatch.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IntelEventPatchCWProxy get copyWith => _$IntelEventPatchCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IntelEventPatch _$IntelEventPatchFromJson(Map<String, dynamic> json) =>
    $checkedCreate('IntelEventPatch', json, ($checkedConvert) {
      final val = IntelEventPatch(
        title: $checkedConvert('title', (v) => v as String?),
        status: $checkedConvert(
          'status',
          (v) => $enumDecodeNullable(_$IntelEventPatchStatusEnumEnumMap, v),
        ),
        severity: $checkedConvert(
          'severity',
          (v) => $enumDecodeNullable(_$SeverityEnumMap, v),
        ),
        notes: $checkedConvert('notes', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$IntelEventPatchToJson(IntelEventPatch instance) =>
    <String, dynamic>{
      'title': ?instance.title,
      'status': ?_$IntelEventPatchStatusEnumEnumMap[instance.status],
      'severity': ?_$SeverityEnumMap[instance.severity],
      'notes': ?instance.notes,
    };

const _$IntelEventPatchStatusEnumEnumMap = {
  IntelEventPatchStatusEnum.emerging: 'emerging',
  IntelEventPatchStatusEnum.active: 'active',
  IntelEventPatchStatusEnum.resolved: 'resolved',
  IntelEventPatchStatusEnum.dismissed: 'dismissed',
};

const _$SeverityEnumMap = {
  Severity.info: 'info',
  Severity.warning: 'warning',
  Severity.critical: 'critical',
};
