// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'context_summary_activity_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ContextSummaryActivityInnerCWProxy {
  ContextSummaryActivityInner at(DateTime at);

  ContextSummaryActivityInner ref(String ref);

  ContextSummaryActivityInner type(ContextSummaryActivityInnerTypeEnum type);

  ContextSummaryActivityInner title(String title);

  ContextSummaryActivityInner severity(String? severity);

  ContextSummaryActivityInner signalFamily(String? signalFamily);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ContextSummaryActivityInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ContextSummaryActivityInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ContextSummaryActivityInner call({
    DateTime at,
    String ref,
    ContextSummaryActivityInnerTypeEnum type,
    String title,
    String? severity,
    String? signalFamily,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfContextSummaryActivityInner.copyWith(...)` or call `instanceOfContextSummaryActivityInner.copyWith.fieldName(value)` for a single field.
class _$ContextSummaryActivityInnerCWProxyImpl
    implements _$ContextSummaryActivityInnerCWProxy {
  const _$ContextSummaryActivityInnerCWProxyImpl(this._value);

  final ContextSummaryActivityInner _value;

  @override
  ContextSummaryActivityInner at(DateTime at) => call(at: at);

  @override
  ContextSummaryActivityInner ref(String ref) => call(ref: ref);

  @override
  ContextSummaryActivityInner type(ContextSummaryActivityInnerTypeEnum type) =>
      call(type: type);

  @override
  ContextSummaryActivityInner title(String title) => call(title: title);

  @override
  ContextSummaryActivityInner severity(String? severity) =>
      call(severity: severity);

  @override
  ContextSummaryActivityInner signalFamily(String? signalFamily) =>
      call(signalFamily: signalFamily);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ContextSummaryActivityInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ContextSummaryActivityInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ContextSummaryActivityInner call({
    Object? at = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? severity = const $CopyWithPlaceholder(),
    Object? signalFamily = const $CopyWithPlaceholder(),
  }) {
    return ContextSummaryActivityInner(
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ContextSummaryActivityInnerTypeEnum,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      severity: severity == const $CopyWithPlaceholder()
          ? _value.severity
          // ignore: cast_nullable_to_non_nullable
          : severity as String?,
      signalFamily: signalFamily == const $CopyWithPlaceholder()
          ? _value.signalFamily
          // ignore: cast_nullable_to_non_nullable
          : signalFamily as String?,
    );
  }
}

extension $ContextSummaryActivityInnerCopyWith on ContextSummaryActivityInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfContextSummaryActivityInner.copyWith(...)` or `instanceOfContextSummaryActivityInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ContextSummaryActivityInnerCWProxy get copyWith =>
      _$ContextSummaryActivityInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ContextSummaryActivityInner _$ContextSummaryActivityInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ContextSummaryActivityInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['at', 'ref', 'type', 'title']);
  final val = ContextSummaryActivityInner(
    at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
    ref: $checkedConvert('ref', (v) => v as String),
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$ContextSummaryActivityInnerTypeEnumEnumMap, v),
    ),
    title: $checkedConvert('title', (v) => v as String),
    severity: $checkedConvert('severity', (v) => v as String?),
    signalFamily: $checkedConvert('signalFamily', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$ContextSummaryActivityInnerToJson(
  ContextSummaryActivityInner instance,
) => <String, dynamic>{
  'at': instance.at.toIso8601String(),
  'ref': instance.ref,
  'type': _$ContextSummaryActivityInnerTypeEnumEnumMap[instance.type]!,
  'title': instance.title,
  'severity': ?instance.severity,
  'signalFamily': ?instance.signalFamily,
};

const _$ContextSummaryActivityInnerTypeEnumEnumMap = {
  ContextSummaryActivityInnerTypeEnum.alert: 'alert',
  ContextSummaryActivityInnerTypeEnum.finding: 'finding',
  ContextSummaryActivityInnerTypeEnum.situationChange: 'situation-change',
  ContextSummaryActivityInnerTypeEnum.record: 'record',
};
