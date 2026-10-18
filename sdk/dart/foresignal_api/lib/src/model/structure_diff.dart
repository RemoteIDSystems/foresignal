//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/structure_diff_presence_dropped_inner.dart';
import 'package:foresignal_api/src/model/structure_diff_delimiter_changed.dart';
import 'package:foresignal_api/src/model/structure_diff_type_changed_inner.dart';
import 'package:foresignal_api/src/model/structure_diff_rename_candidates_inner.dart';
import 'package:foresignal_api/src/model/structure_diff_content_type_changed.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'structure_diff.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class StructureDiff {
  /// Returns a new [StructureDiff] instance.
  StructureDiff({

    required  this.format,

     this.fixtureFile,

    required  this.addedPaths,

    required  this.removedPaths,

    required  this.typeChanged,

     this.presenceDropped,

     this.arrayEmpty,

     this.columnsReordered,

     this.delimiterChanged,

     this.contentTypeChanged,

     this.sizeRatio,

     this.renameCandidates,

     this.htmlTitle,

     this.examples,
  });

  @JsonKey(
    
    name: r'format',
    required: true,
    includeIfNull: false,
  )


  final StructureDiffFormatEnum format;



  @JsonKey(
    
    name: r'fixtureFile',
    required: false,
    includeIfNull: false,
  )


  final String? fixtureFile;



  @JsonKey(
    
    name: r'addedPaths',
    required: true,
    includeIfNull: false,
  )


  final List<String> addedPaths;



  @JsonKey(
    
    name: r'removedPaths',
    required: true,
    includeIfNull: false,
  )


  final List<String> removedPaths;



  @JsonKey(
    
    name: r'typeChanged',
    required: true,
    includeIfNull: false,
  )


  final List<StructureDiffTypeChangedInner> typeChanged;



  @JsonKey(
    
    name: r'presenceDropped',
    required: false,
    includeIfNull: false,
  )


  final List<StructureDiffPresenceDroppedInner>? presenceDropped;



  @JsonKey(
    
    name: r'arrayEmpty',
    required: false,
    includeIfNull: false,
  )


  final List<String>? arrayEmpty;



  @JsonKey(
    
    name: r'columnsReordered',
    required: false,
    includeIfNull: false,
  )


  final bool? columnsReordered;



  @JsonKey(
    
    name: r'delimiterChanged',
    required: false,
    includeIfNull: false,
  )


  final StructureDiffDelimiterChanged? delimiterChanged;



  @JsonKey(
    
    name: r'contentTypeChanged',
    required: false,
    includeIfNull: false,
  )


  final StructureDiffContentTypeChanged? contentTypeChanged;



  @JsonKey(
    
    name: r'sizeRatio',
    required: false,
    includeIfNull: false,
  )


  final num? sizeRatio;



  @JsonKey(
    
    name: r'renameCandidates',
    required: false,
    includeIfNull: false,
  )


  final List<StructureDiffRenameCandidatesInner>? renameCandidates;



      /// `<title>` when HTML arrived instead of data
  @JsonKey(
    
    name: r'htmlTitle',
    required: false,
    includeIfNull: false,
  )


  final String? htmlTitle;



      /// Path → up to 3 capped example values
  @JsonKey(
    
    name: r'examples',
    required: false,
    includeIfNull: false,
  )


  final Map<String, List<String>>? examples;





    @override
    bool operator ==(Object other) => identical(this, other) || other is StructureDiff &&
      other.format == format &&
      other.fixtureFile == fixtureFile &&
      other.addedPaths == addedPaths &&
      other.removedPaths == removedPaths &&
      other.typeChanged == typeChanged &&
      other.presenceDropped == presenceDropped &&
      other.arrayEmpty == arrayEmpty &&
      other.columnsReordered == columnsReordered &&
      other.delimiterChanged == delimiterChanged &&
      other.contentTypeChanged == contentTypeChanged &&
      other.sizeRatio == sizeRatio &&
      other.renameCandidates == renameCandidates &&
      other.htmlTitle == htmlTitle &&
      other.examples == examples;

    @override
    int get hashCode =>
        format.hashCode +
        (fixtureFile == null ? 0 : fixtureFile.hashCode) +
        addedPaths.hashCode +
        removedPaths.hashCode +
        typeChanged.hashCode +
        presenceDropped.hashCode +
        arrayEmpty.hashCode +
        columnsReordered.hashCode +
        (delimiterChanged == null ? 0 : delimiterChanged.hashCode) +
        (contentTypeChanged == null ? 0 : contentTypeChanged.hashCode) +
        (sizeRatio == null ? 0 : sizeRatio.hashCode) +
        renameCandidates.hashCode +
        (htmlTitle == null ? 0 : htmlTitle.hashCode) +
        examples.hashCode;

  factory StructureDiff.fromJson(Map<String, dynamic> json) => _$StructureDiffFromJson(json);

  Map<String, dynamic> toJson() => _$StructureDiffToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum StructureDiffFormatEnum {
@JsonValue(r'json')
json(r'json'),
@JsonValue(r'csv')
csv(r'csv'),
@JsonValue(r'tsv')
tsv(r'tsv'),
@JsonValue(r'xml')
xml(r'xml'),
@JsonValue(r'binary')
binary(r'binary'),
@JsonValue(r'html')
html(r'html'),
@JsonValue(r'text')
text(r'text');

const StructureDiffFormatEnum(this.value);

final String value;

@override
String toString() => value;
}


