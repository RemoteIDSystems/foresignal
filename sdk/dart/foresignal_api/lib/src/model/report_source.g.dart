// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'report_source.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReportSourceCWProxy {
  ReportSource type(ReportSourceTypeEnum type);

  ReportSource ref(String? ref);

  ReportSource refs(List<String>? refs);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportSource(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportSource(...).copyWith(id: 12, name: "My name")
  /// ```
  ReportSource call({
    ReportSourceTypeEnum type,
    String? ref,
    List<String>? refs,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReportSource.copyWith(...)` or call `instanceOfReportSource.copyWith.fieldName(value)` for a single field.
class _$ReportSourceCWProxyImpl implements _$ReportSourceCWProxy {
  const _$ReportSourceCWProxyImpl(this._value);

  final ReportSource _value;

  @override
  ReportSource type(ReportSourceTypeEnum type) => call(type: type);

  @override
  ReportSource ref(String? ref) => call(ref: ref);

  @override
  ReportSource refs(List<String>? refs) => call(refs: refs);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportSource(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportSource(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReportSource call({
    Object? type = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
    Object? refs = const $CopyWithPlaceholder(),
  }) {
    return ReportSource(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ReportSourceTypeEnum,
      ref: ref == const $CopyWithPlaceholder()
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String?,
      refs: refs == const $CopyWithPlaceholder()
          ? _value.refs
          // ignore: cast_nullable_to_non_nullable
          : refs as List<String>?,
    );
  }
}

extension $ReportSourceCopyWith on ReportSource {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReportSource.copyWith(...)` or `instanceOfReportSource.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReportSourceCWProxy get copyWith => _$ReportSourceCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReportSource _$ReportSourceFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ReportSource', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['type']);
      final val = ReportSource(
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$ReportSourceTypeEnumEnumMap, v),
        ),
        ref: $checkedConvert('ref', (v) => v as String?),
        refs: $checkedConvert(
          'refs',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ReportSourceToJson(ReportSource instance) =>
    <String, dynamic>{
      'type': _$ReportSourceTypeEnumEnumMap[instance.type]!,
      'ref': ?instance.ref,
      'refs': ?instance.refs,
    };

const _$ReportSourceTypeEnumEnumMap = {
  ReportSourceTypeEnum.selection: 'selection',
  ReportSourceTypeEnum.tag: 'tag',
  ReportSourceTypeEnum.investigation: 'investigation',
  ReportSourceTypeEnum.chronicle: 'chronicle',
  ReportSourceTypeEnum.situation: 'situation',
  ReportSourceTypeEnum.workspace: 'workspace',
};
