// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'report_block_one_of2_all_of_items_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReportBlockOneOf2AllOfItemsInnerCWProxy {
  ReportBlockOneOf2AllOfItemsInner ref(String ref);

  ReportBlockOneOf2AllOfItemsInner envelope(ObjectEnvelope? envelope);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportBlockOneOf2AllOfItemsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportBlockOneOf2AllOfItemsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ReportBlockOneOf2AllOfItemsInner call({String ref, ObjectEnvelope? envelope});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReportBlockOneOf2AllOfItemsInner.copyWith(...)` or call `instanceOfReportBlockOneOf2AllOfItemsInner.copyWith.fieldName(value)` for a single field.
class _$ReportBlockOneOf2AllOfItemsInnerCWProxyImpl
    implements _$ReportBlockOneOf2AllOfItemsInnerCWProxy {
  const _$ReportBlockOneOf2AllOfItemsInnerCWProxyImpl(this._value);

  final ReportBlockOneOf2AllOfItemsInner _value;

  @override
  ReportBlockOneOf2AllOfItemsInner ref(String ref) => call(ref: ref);

  @override
  ReportBlockOneOf2AllOfItemsInner envelope(ObjectEnvelope? envelope) =>
      call(envelope: envelope);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportBlockOneOf2AllOfItemsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportBlockOneOf2AllOfItemsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReportBlockOneOf2AllOfItemsInner call({
    Object? ref = const $CopyWithPlaceholder(),
    Object? envelope = const $CopyWithPlaceholder(),
  }) {
    return ReportBlockOneOf2AllOfItemsInner(
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      envelope: envelope == const $CopyWithPlaceholder()
          ? _value.envelope
          // ignore: cast_nullable_to_non_nullable
          : envelope as ObjectEnvelope?,
    );
  }
}

extension $ReportBlockOneOf2AllOfItemsInnerCopyWith
    on ReportBlockOneOf2AllOfItemsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReportBlockOneOf2AllOfItemsInner.copyWith(...)` or `instanceOfReportBlockOneOf2AllOfItemsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReportBlockOneOf2AllOfItemsInnerCWProxy get copyWith =>
      _$ReportBlockOneOf2AllOfItemsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReportBlockOneOf2AllOfItemsInner _$ReportBlockOneOf2AllOfItemsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ReportBlockOneOf2AllOfItemsInner', json, (
  $checkedConvert,
) {
  $checkKeys(json, requiredKeys: const ['ref']);
  final val = ReportBlockOneOf2AllOfItemsInner(
    ref: $checkedConvert('ref', (v) => v as String),
    envelope: $checkedConvert(
      'envelope',
      (v) =>
          v == null ? null : ObjectEnvelope.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$ReportBlockOneOf2AllOfItemsInnerToJson(
  ReportBlockOneOf2AllOfItemsInner instance,
) => <String, dynamic>{
  'ref': instance.ref,
  'envelope': ?instance.envelope?.toJson(),
};
