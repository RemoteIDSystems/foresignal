//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'report_source.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReportSource {
  /// Returns a new [ReportSource] instance.
  ReportSource({

    required  this.type,

     this.ref,

     this.refs,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final ReportSourceTypeEnum type;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'ref',
    required: false,
    includeIfNull: false,
  )


  final String? ref;



      /// For `selection`
  @JsonKey(
    
    name: r'refs',
    required: false,
    includeIfNull: false,
  )


  final List<String>? refs;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReportSource &&
      other.type == type &&
      other.ref == ref &&
      other.refs == refs;

    @override
    int get hashCode =>
        type.hashCode +
        (ref == null ? 0 : ref.hashCode) +
        refs.hashCode;

  factory ReportSource.fromJson(Map<String, dynamic> json) => _$ReportSourceFromJson(json);

  Map<String, dynamic> toJson() => _$ReportSourceToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ReportSourceTypeEnum {
@JsonValue(r'selection')
selection(r'selection'),
@JsonValue(r'tag')
tag(r'tag'),
@JsonValue(r'investigation')
investigation(r'investigation'),
@JsonValue(r'chronicle')
chronicle(r'chronicle'),
@JsonValue(r'situation')
situation(r'situation'),
@JsonValue(r'workspace')
workspace(r'workspace');

const ReportSourceTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


