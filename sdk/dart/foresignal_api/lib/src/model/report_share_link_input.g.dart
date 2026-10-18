// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'report_share_link_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReportShareLinkInputCWProxy {
  ReportShareLinkInput expiresAt(DateTime? expiresAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportShareLinkInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportShareLinkInput(...).copyWith(id: 12, name: "My name")
  /// ```
  ReportShareLinkInput call({DateTime? expiresAt});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReportShareLinkInput.copyWith(...)` or call `instanceOfReportShareLinkInput.copyWith.fieldName(value)` for a single field.
class _$ReportShareLinkInputCWProxyImpl
    implements _$ReportShareLinkInputCWProxy {
  const _$ReportShareLinkInputCWProxyImpl(this._value);

  final ReportShareLinkInput _value;

  @override
  ReportShareLinkInput expiresAt(DateTime? expiresAt) =>
      call(expiresAt: expiresAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportShareLinkInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportShareLinkInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReportShareLinkInput call({
    Object? expiresAt = const $CopyWithPlaceholder(),
  }) {
    return ReportShareLinkInput(
      expiresAt: expiresAt == const $CopyWithPlaceholder()
          ? _value.expiresAt
          // ignore: cast_nullable_to_non_nullable
          : expiresAt as DateTime?,
    );
  }
}

extension $ReportShareLinkInputCopyWith on ReportShareLinkInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReportShareLinkInput.copyWith(...)` or `instanceOfReportShareLinkInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReportShareLinkInputCWProxy get copyWith =>
      _$ReportShareLinkInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReportShareLinkInput _$ReportShareLinkInputFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ReportShareLinkInput', json, ($checkedConvert) {
  final val = ReportShareLinkInput(
    expiresAt: $checkedConvert(
      'expiresAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$ReportShareLinkInputToJson(
  ReportShareLinkInput instance,
) => <String, dynamic>{'expiresAt': ?instance.expiresAt?.toIso8601String()};
