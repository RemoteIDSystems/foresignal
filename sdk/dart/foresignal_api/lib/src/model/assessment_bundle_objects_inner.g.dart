// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'assessment_bundle_objects_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AssessmentBundleObjectsInnerCWProxy {
  AssessmentBundleObjectsInner ref(String ref);

  AssessmentBundleObjectsInner envelope(ObjectEnvelope envelope);

  AssessmentBundleObjectsInner data(Map<String, Object>? data);

  AssessmentBundleObjectsInner provenance(Provenance? provenance);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AssessmentBundleObjectsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AssessmentBundleObjectsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  AssessmentBundleObjectsInner call({
    String ref,
    ObjectEnvelope envelope,
    Map<String, Object>? data,
    Provenance? provenance,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAssessmentBundleObjectsInner.copyWith(...)` or call `instanceOfAssessmentBundleObjectsInner.copyWith.fieldName(value)` for a single field.
class _$AssessmentBundleObjectsInnerCWProxyImpl
    implements _$AssessmentBundleObjectsInnerCWProxy {
  const _$AssessmentBundleObjectsInnerCWProxyImpl(this._value);

  final AssessmentBundleObjectsInner _value;

  @override
  AssessmentBundleObjectsInner ref(String ref) => call(ref: ref);

  @override
  AssessmentBundleObjectsInner envelope(ObjectEnvelope envelope) =>
      call(envelope: envelope);

  @override
  AssessmentBundleObjectsInner data(Map<String, Object>? data) =>
      call(data: data);

  @override
  AssessmentBundleObjectsInner provenance(Provenance? provenance) =>
      call(provenance: provenance);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AssessmentBundleObjectsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AssessmentBundleObjectsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AssessmentBundleObjectsInner call({
    Object? ref = const $CopyWithPlaceholder(),
    Object? envelope = const $CopyWithPlaceholder(),
    Object? data = const $CopyWithPlaceholder(),
    Object? provenance = const $CopyWithPlaceholder(),
  }) {
    return AssessmentBundleObjectsInner(
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      envelope: envelope == const $CopyWithPlaceholder() || envelope == null
          ? _value.envelope
          // ignore: cast_nullable_to_non_nullable
          : envelope as ObjectEnvelope,
      data: data == const $CopyWithPlaceholder()
          ? _value.data
          // ignore: cast_nullable_to_non_nullable
          : data as Map<String, Object>?,
      provenance: provenance == const $CopyWithPlaceholder()
          ? _value.provenance
          // ignore: cast_nullable_to_non_nullable
          : provenance as Provenance?,
    );
  }
}

extension $AssessmentBundleObjectsInnerCopyWith
    on AssessmentBundleObjectsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAssessmentBundleObjectsInner.copyWith(...)` or `instanceOfAssessmentBundleObjectsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AssessmentBundleObjectsInnerCWProxy get copyWith =>
      _$AssessmentBundleObjectsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AssessmentBundleObjectsInner _$AssessmentBundleObjectsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AssessmentBundleObjectsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['ref', 'envelope']);
  final val = AssessmentBundleObjectsInner(
    ref: $checkedConvert('ref', (v) => v as String),
    envelope: $checkedConvert(
      'envelope',
      (v) => ObjectEnvelope.fromJson(v as Map<String, dynamic>),
    ),
    data: $checkedConvert(
      'data',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    provenance: $checkedConvert(
      'provenance',
      (v) => v == null ? null : Provenance.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$AssessmentBundleObjectsInnerToJson(
  AssessmentBundleObjectsInner instance,
) => <String, dynamic>{
  'ref': instance.ref,
  'envelope': instance.envelope.toJson(),
  'data': ?instance.data,
  'provenance': ?instance.provenance?.toJson(),
};
