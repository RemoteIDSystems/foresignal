// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'report_block_one_of4_all_of_items_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReportBlockOneOf4AllOfItemsInnerCWProxy {
  ReportBlockOneOf4AllOfItemsInner at(DateTime at);

  ReportBlockOneOf4AllOfItemsInner title(String title);

  ReportBlockOneOf4AllOfItemsInner ref(String? ref);

  ReportBlockOneOf4AllOfItemsInner envelope(ObjectEnvelope? envelope);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportBlockOneOf4AllOfItemsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportBlockOneOf4AllOfItemsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ReportBlockOneOf4AllOfItemsInner call({
    DateTime at,
    String title,
    String? ref,
    ObjectEnvelope? envelope,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReportBlockOneOf4AllOfItemsInner.copyWith(...)` or call `instanceOfReportBlockOneOf4AllOfItemsInner.copyWith.fieldName(value)` for a single field.
class _$ReportBlockOneOf4AllOfItemsInnerCWProxyImpl
    implements _$ReportBlockOneOf4AllOfItemsInnerCWProxy {
  const _$ReportBlockOneOf4AllOfItemsInnerCWProxyImpl(this._value);

  final ReportBlockOneOf4AllOfItemsInner _value;

  @override
  ReportBlockOneOf4AllOfItemsInner at(DateTime at) => call(at: at);

  @override
  ReportBlockOneOf4AllOfItemsInner title(String title) => call(title: title);

  @override
  ReportBlockOneOf4AllOfItemsInner ref(String? ref) => call(ref: ref);

  @override
  ReportBlockOneOf4AllOfItemsInner envelope(ObjectEnvelope? envelope) =>
      call(envelope: envelope);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportBlockOneOf4AllOfItemsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportBlockOneOf4AllOfItemsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReportBlockOneOf4AllOfItemsInner call({
    Object? at = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
    Object? envelope = const $CopyWithPlaceholder(),
  }) {
    return ReportBlockOneOf4AllOfItemsInner(
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      ref: ref == const $CopyWithPlaceholder()
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String?,
      envelope: envelope == const $CopyWithPlaceholder()
          ? _value.envelope
          // ignore: cast_nullable_to_non_nullable
          : envelope as ObjectEnvelope?,
    );
  }
}

extension $ReportBlockOneOf4AllOfItemsInnerCopyWith
    on ReportBlockOneOf4AllOfItemsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReportBlockOneOf4AllOfItemsInner.copyWith(...)` or `instanceOfReportBlockOneOf4AllOfItemsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReportBlockOneOf4AllOfItemsInnerCWProxy get copyWith =>
      _$ReportBlockOneOf4AllOfItemsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReportBlockOneOf4AllOfItemsInner _$ReportBlockOneOf4AllOfItemsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ReportBlockOneOf4AllOfItemsInner', json, (
  $checkedConvert,
) {
  $checkKeys(json, requiredKeys: const ['at', 'title']);
  final val = ReportBlockOneOf4AllOfItemsInner(
    at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
    title: $checkedConvert('title', (v) => v as String),
    ref: $checkedConvert('ref', (v) => v as String?),
    envelope: $checkedConvert(
      'envelope',
      (v) =>
          v == null ? null : ObjectEnvelope.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$ReportBlockOneOf4AllOfItemsInnerToJson(
  ReportBlockOneOf4AllOfItemsInner instance,
) => <String, dynamic>{
  'at': instance.at.toIso8601String(),
  'title': instance.title,
  'ref': ?instance.ref,
  'envelope': ?instance.envelope?.toJson(),
};
