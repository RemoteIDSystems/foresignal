// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'intel_board_situations_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IntelBoardSituationsInnerCWProxy {
  IntelBoardSituationsInner id(String id);

  IntelBoardSituationsInner title(String title);

  IntelBoardSituationsInner status(String? status);

  IntelBoardSituationsInner severity(Severity severity);

  IntelBoardSituationsInner lastActivityAt(DateTime? lastActivityAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelBoardSituationsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelBoardSituationsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  IntelBoardSituationsInner call({
    String id,
    String title,
    String? status,
    Severity severity,
    DateTime? lastActivityAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIntelBoardSituationsInner.copyWith(...)` or call `instanceOfIntelBoardSituationsInner.copyWith.fieldName(value)` for a single field.
class _$IntelBoardSituationsInnerCWProxyImpl
    implements _$IntelBoardSituationsInnerCWProxy {
  const _$IntelBoardSituationsInnerCWProxyImpl(this._value);

  final IntelBoardSituationsInner _value;

  @override
  IntelBoardSituationsInner id(String id) => call(id: id);

  @override
  IntelBoardSituationsInner title(String title) => call(title: title);

  @override
  IntelBoardSituationsInner status(String? status) => call(status: status);

  @override
  IntelBoardSituationsInner severity(Severity severity) =>
      call(severity: severity);

  @override
  IntelBoardSituationsInner lastActivityAt(DateTime? lastActivityAt) =>
      call(lastActivityAt: lastActivityAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelBoardSituationsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelBoardSituationsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IntelBoardSituationsInner call({
    Object? id = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? severity = const $CopyWithPlaceholder(),
    Object? lastActivityAt = const $CopyWithPlaceholder(),
  }) {
    return IntelBoardSituationsInner(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      status: status == const $CopyWithPlaceholder()
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as String?,
      severity: severity == const $CopyWithPlaceholder() || severity == null
          ? _value.severity
          // ignore: cast_nullable_to_non_nullable
          : severity as Severity,
      lastActivityAt: lastActivityAt == const $CopyWithPlaceholder()
          ? _value.lastActivityAt
          // ignore: cast_nullable_to_non_nullable
          : lastActivityAt as DateTime?,
    );
  }
}

extension $IntelBoardSituationsInnerCopyWith on IntelBoardSituationsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIntelBoardSituationsInner.copyWith(...)` or `instanceOfIntelBoardSituationsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IntelBoardSituationsInnerCWProxy get copyWith =>
      _$IntelBoardSituationsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IntelBoardSituationsInner _$IntelBoardSituationsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IntelBoardSituationsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['id', 'title', 'severity']);
  final val = IntelBoardSituationsInner(
    id: $checkedConvert('id', (v) => v as String),
    title: $checkedConvert('title', (v) => v as String),
    status: $checkedConvert('status', (v) => v as String?),
    severity: $checkedConvert(
      'severity',
      (v) => $enumDecode(_$SeverityEnumMap, v),
    ),
    lastActivityAt: $checkedConvert(
      'lastActivityAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$IntelBoardSituationsInnerToJson(
  IntelBoardSituationsInner instance,
) => <String, dynamic>{
  'id': instance.id,
  'title': instance.title,
  'status': ?instance.status,
  'severity': _$SeverityEnumMap[instance.severity]!,
  'lastActivityAt': ?instance.lastActivityAt?.toIso8601String(),
};

const _$SeverityEnumMap = {
  Severity.info: 'info',
  Severity.warning: 'warning',
  Severity.critical: 'critical',
};
