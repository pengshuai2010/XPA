/*++
Copyright (c) 2006 Microsoft Corporation

Module Name:

    symbol.cpp

Abstract:
 
    Lisp-like symbols.

Author:

    Leonardo de Moura (leonardo) 2006-09-11.

Revision History:

--*/
#include"symbol.h"
#include"str_hashtable.h"
#include"region.h"
#include"string_buffer.h"
#include"z3_omp.h"

symbol symbol::m_dummy(TAG(void*, static_cast<void*>(0), 2));
const symbol symbol::null;

/**
   \brief Symbol table manager. It stores the symbol strings created at runtime.
*/
class internal_symbol_table {
    region        m_region; //!< Region used to store symbol strings.
    str_hashtable m_table;  //!< Table of created symbol strings.
public:

    char const * get_str(char const * d) {
        char * result;
        #pragma omp critical (cr_symbol) 
        {
        char * r_d = const_cast<char *>(d);
        str_hashtable::entry * e;
        if (m_table.insert_if_not_there_core(r_d, e)) {
            // new entry
            size_t l   = strlen(d);
            // store the hash-code before the string
            size_t * mem = static_cast<size_t*>(m_region.allocate(l + 1 + sizeof(size_t)));
            *mem = e->get_hash();
            mem++;
            result = reinterpret_cast<char*>(mem);
            memcpy(result, d, l+1);
            // update the entry with the new ptr.
            e->set_data(result);
        }
        else {
            result = e->get_data();
        }
        SASSERT(m_table.contains(result));
        }
        return result;
    }
};

internal_symbol_table* g_symbol_table = 0;

void initialize_symbols() {
    if (!g_symbol_table) {
        g_symbol_table = alloc(internal_symbol_table);
    }
}

void finalize_symbols() {
    dealloc(g_symbol_table);
    g_symbol_table = 0;
}

symbol::symbol(char const * d) {
    if (d == 0)
        m_data = 0;
    else
        m_data = g_symbol_table->get_str(d);
}

symbol & symbol::operator=(char const * d) {
    m_data = g_symbol_table->get_str(d);
    return *this;
}

std::string symbol::str() const {
    SASSERT(!is_marked());
    if (GET_TAG(m_data) == 0) { 
        if (m_data)
            return m_data;
        else
            return "<null>";
    }
    else {
        string_buffer<128> buffer;
        buffer << "k!" << UNBOXINT(m_data);
        return buffer.c_str();
    }
}

bool symbol::contains(char ch) const {
    SASSERT(!is_marked());
    if (GET_TAG(m_data) == 0) {
        return strchr(m_data, ch) != 0;
    }
    else {
        return false;
    }
}
 
unsigned symbol::size() const {
    SASSERT(!is_marked());
    if (GET_TAG(m_data) == 0) {
        return static_cast<unsigned>(strlen(m_data));
    }
    else {
        unsigned v = UNBOXINT(m_data);
        unsigned sz = 4;
        v = v >> 1;
        while (v > 0) {
            sz++;
            v = v >> 1;
        }
        return sz;
    }
}

bool lt(symbol const & s1, symbol const & s2) { 
    if (s1 == s2)
        return false;
    if (s1.is_numerical()) {
        if (!s2.is_numerical())
            return true; // numeral symbols are smaller than non-numerical ones.
        return s1.get_num() < s2.get_num();
    }
    if (s2.is_numerical()) {
        SASSERT(!s1.is_numerical());
        return false;
    }
    SASSERT(!s1.is_numerical() && !s2.is_numerical());
    char const * str1 = s1.bare_str();
    char const * str2 = s2.bare_str();
    while (true) {
        if (*str1 < *str2) {
            return true;
        }
        else if (*str1 == *str2) {
            str1++;
            str2++;
            if (!*str1) {
                SASSERT(*str2); // the strings can't be equal.
                return true;
            }
            if (!*str2) {
                SASSERT(*str1); // the strings can't be equal.
                return false;
            }
        }
        else {
            SASSERT(*str1 > *str2);
            return false;
        }
    }
}
